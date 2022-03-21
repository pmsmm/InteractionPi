package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device;

import com.fazecast.jSerialComm.SerialPort;
import org.springframework.web.multipart.MultipartFile;
import pt.ulisboa.tecnico.interactionpiprocess.exceptions.InvalidFileSpecificationException;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.Commands;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.SerialDeviceResponse;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.handlers.BaudRateAck;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.handlers.Setup;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.handlers.StartAck;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.handlers.StopAck;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.port.handlers.ComPortListener;

import java.io.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Represents a Serial Device and contains all of the common methods and must have methods that either type of serial device
 * (Interaction or Sensor) should contain.
 *
 * @author Pedro Maria
 * @version 1.0
 * @since 1.0
 */

public abstract class SerialDevice {

    protected final static Logger LOGGER = Logger.getLogger(SerialDevice.class.getName());
    protected final ExecutorService threadPool = Executors.newFixedThreadPool(3);
    protected final ConcurrentHashMap<String, SerialDeviceResponse> responses = new ConcurrentHashMap<>();

    final SerialPort serialPort;
    private final InputStream serialPortInputStream;
    private final OutputStream serialPortOutputStream;

    Identification id;
    File schematic;
    String description;
    boolean running;
    String deviceLocation = "Location Not Set";
    public boolean SETUP_COMPLETE = false;

    protected SerialDevice(SerialPort serialPort) throws IOException {
        LOGGER.info("Opening Serial Device's Port");
        this.serialPort = serialPort;
        this.serialPort.openPort(2000);
        this.serialPortInputStream = serialPort.getInputStream();
        this.serialPortOutputStream = serialPort.getOutputStream();
        LOGGER.finest("Skipped over " + this.serialPortInputStream.skip(serialPortInputStream.available()) + "bytes");
        this.serialPort.addDataListener(new ComPortListener());
        this.id = new Identification("", serialPort.getSystemPortName());
        LOGGER.info("Serial Device added successfully");
        threadPool.submit(new Setup(this));
    }

    /**
     * Sends the SETUP command to the serial device.
     *
     * @throws IOException If an error occurs while trying to write to serial port.
     */
    public final void setup() throws IOException {
        LOGGER.info("Sending SETUP Command ...");
        writeToSerial(Commands.SETUP);
    }

    /**
     * Sends the START command to the serial device.
     *
     * @return The message returned by the serial device acknowledging the start command.
     * @throws IOException If an error occurs while trying to write to serial port.
     * @throws InterruptedException If the thread gets interrupted during its execution.
     * @throws ExecutionException If an error occurs during thread execution while its waiting for an answer from the device.
     * @throws TimeoutException If the thread waiting for a response from the device times out.
     */
    public final String start() throws IOException, InterruptedException, ExecutionException, TimeoutException{
        LOGGER.info("Sending START command to " + serialPort.getSystemPortName());
        Future<String> response = threadPool.submit(new StartAck(responses, writeToSerial(Commands.START)));
        return response.get(10, TimeUnit.SECONDS);
    }

    /**
     * Sends the STOP command to the serial device.
     *
     * @return The message returned by the serial device acknowledging the stop command.
     * @throws IOException If an error occurs while trying to write to serial port.
     * @throws InterruptedException If the thread gets interrupted during its execution.
     * @throws ExecutionException If an error occurs during thread execution while its waiting for an answer from the device.
     * @throws TimeoutException If the thread waiting for a response from the device times out.
     */
    public final String stop() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        LOGGER.info("Sending STOP command to " + serialPort.getSystemPortName());
        Future<String> response = threadPool.submit(new StopAck(responses, writeToSerial(Commands.STOP)));
        return response.get(10, TimeUnit.SECONDS);
    }

    /**
     * Method used to ping the serial device and check if it is still answering commands.
     *
     * @return The message returned by the device acknowledging the ping command.
     * @throws IOException If an error occurs while trying to write to serial port.
     */
    public synchronized final String ping() throws IOException {
        LOGGER.fine("Pinging device at " + serialPort.getSystemPortName());
        return writeToSerial(Commands.PING);
    }

    /**
     * Changes the baud rate at which the serial is running both in the interaction pi and in the device itself.
     *
     * @param baudRate The new baud rate at which to read from/write to the serial port
     * @return Message acknowledging the operation
     * @throws IOException If an error occurs while trying to write to serial port.
     * @throws InterruptedException If the thread gets interrupted during its execution.
     * @throws ExecutionException If an error occurs during thread execution while its waiting for an answer from the device.
     * @throws TimeoutException If the thread waiting for a response from the device times out.
     */
    public final String baud(int baudRate) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        if (isRunning()) {
            return "The Serial Device cannot be changed while the device is running. Please STOP the device first.";
        } else {
            LOGGER.info("Sending BAUD command to " + serialPort.getSystemPortName());
            Future<String> response = threadPool.submit(new BaudRateAck(responses, writeToSerial(Commands.BAUD)));
            String deviceResponse = response.get(5, TimeUnit.SECONDS);
            serialPort.setBaudRate(baudRate);
            return deviceResponse;
        }
    }

    /**
     * Used to write to the serial port.
     *
     * @param command The command to send to the serial port.
     * @return Request identifier so that the thread can wait for a response containing this identifier.
     * @throws IOException If an error occurs while trying to write to serial port.
     */
    public synchronized String writeToSerial(Commands command) throws IOException {
        try {
            String id = String.valueOf(System.currentTimeMillis());
            StringBuilder message = new StringBuilder("COM:" + command.toString() + ";");
            message.append("ID:").append(id).append("\n");
            serialPortOutputStream.write(message.toString().getBytes());
            return id;
        }catch (IOException io){
            LOGGER.severe("An Issue occurred while trying to write to serial interface");
            throw io;
        }
    }

    /**
     * Used to write to the serial port.
     *
     * @param command The command to send to the serial port.
     * @param values Values associated to some of the commands.
     * @return Request identifier so that the thread can wait for a response containing this identifier.
     * @throws IOException If an error occurs while trying to write to serial port.
     */
    public synchronized String writeToSerial(Commands command, int... values) throws IOException {
        try {
            String id = String.valueOf(System.currentTimeMillis());
            StringBuilder message = new StringBuilder("COM:" + command.toString() + ";");
            for (int value : values) {
                message.append("VAL:").append(value).append(";");
            }
            message.append("ID:").append(id).append("\n");
            serialPortOutputStream.write(message.toString().getBytes());
            return id;
        }catch (IOException io){
            LOGGER.severe("An Issue occurred while trying to write to serial interface");
            throw io;
        }
    }

    /**
     * Used to write to the serial port.
     *
     * @param command The command to send to the serial port.
     * @param values Values associated to some of the commands.
     * @return Request identifier so that the thread can wait for a response containing this identifier.
     * @throws IOException If an error occurs while trying to write to serial port.
     */
    public synchronized String writeToSerial(Commands command, String... values) throws IOException {
        try {
            String id = String.valueOf(System.currentTimeMillis());
            StringBuilder message = new StringBuilder("COM:" + command.toString() + ";");
            for (String value : values) {
                message.append("VAL:").append(value).append(";");
            }
            message.append("ID:").append(id).append("\n");
            serialPortOutputStream.write(message.toString().getBytes());
            return id;
        }catch (IOException io){
            LOGGER.severe("An Issue occurred while trying to write to serial interface");
            throw io;
        }
    }

    /**
     * Updates the queue of responses from the serial device where thread are waiting to consume those responses.
     *
     * @param ID The ID of the response returned corresponding to a thread's request ID
     * @param response The actual response from the device.
     */
    public void updateResponseQueue(String ID, SerialDeviceResponse response) {
        responses.put(ID, response);
    }

    public abstract void setupDevice(String deviceName, String rate);

    /**
     * Shuts down all streams and ports and cleans up all of the information related to this serial device.
     */
    public void cleanup() {
        try {
            this.id = null;
            this.schematic = null;
            this.description = null;
            LOGGER.info("Closing " + serialPort.getSystemPortName() + " Input and Output Streams.");
            this.serialPortInputStream.close();
            this.serialPortOutputStream.close();
            this.serialPort.removeDataListener();
            this.serialPort.closePort();
            LOGGER.info("Closed " + serialPort.getSystemPortName());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    //###################################### Getters and Setters ##############################################

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public Identification getId() {
        return id;
    }

    public void setId(Identification id) {
        this.id = id;
    }

    public File getSchematic() {
        return schematic;
    }

    /**
     * Sets the schematic of the serial device
     *
     * @param schematic File of the schematic.
     * @throws InvalidFileSpecificationException If the user does not select a PDF file with the information about the device.
     */
    public void setSchematic(MultipartFile schematic) throws InvalidFileSpecificationException {
        if (schematic.isEmpty()) {
            throw new InvalidFileSpecificationException("Please choose a PDF file containing the schematic, code and components of the serial device.");
        }

        if (schematic.getSize() > 20000000) {
            throw new InvalidFileSpecificationException("The file size is to big. Please upload PDF under 20MB");
        }

        if (schematic.getContentType() != null && !schematic.getContentType().equals("application/pdf")) {
            throw new InvalidFileSpecificationException("Invalid File type. Please submit a PDF file.");
        }

        //TODO: Change this path to the device's folder in order to store there the schematic
        File deviceSchematic = new File("Schematic.pdf");
        try (FileOutputStream outputStream = new FileOutputStream(deviceSchematic, false)) {
            LOGGER.info(deviceSchematic.createNewFile() ? "Schematic File created successfully" : "The File already exists, overwriting ...");
            outputStream.write(schematic.getBytes());
            outputStream.flush();
            this.schematic = deviceSchematic;
        } catch (IOException exception) {
            throw new InvalidFileSpecificationException("An error occurred while trying to create the schematic", exception);
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public abstract DeviceType getType();

    public String getDeviceLocation() {
        return deviceLocation;
    }

    //TODO: Input validations should be made here
    public void setDeviceLocation(String deviceLocation) {
        this.deviceLocation = deviceLocation;
    }
}
