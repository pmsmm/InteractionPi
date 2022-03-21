package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device;

import com.fazecast.jSerialComm.SerialPort;
import pt.ulisboa.tecnico.interactionpiprocess.exceptions.NoAvailableSerialDevices;
import pt.ulisboa.tecnico.interactionpiprocess.exceptions.NoSuchSerialDeviceException;
import pt.ulisboa.tecnico.interactionpiprocess.exceptions.SingletonException;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.dto.SerialDeviceDTO;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.dto.SerialPortPlaceHolder;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.port.handlers.SerialPortsHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class SerialDevicesHandler {

    private static final ConcurrentHashMap<String, SerialDevice> serialDevicesHashMap = new ConcurrentHashMap<>();
    private static SerialDevicesHandler INSTANCE;

    private SerialDevicesHandler() throws SingletonException {
        if (INSTANCE != null) {
            throw new SingletonException();
        }
    }

    public static SerialDevicesHandler getInstance() throws SingletonException {
        if(INSTANCE == null) {
            INSTANCE = new SerialDevicesHandler();
        }
        return INSTANCE;
    }

    /**
     * Gets all of the Serial Devices (All types) connected to this Interaction Pi.
     *
     * @return An Hashmap containing the port names as keys and the SerialDevice objects as values.
     */
    public static ConcurrentHashMap<String, SerialDevice> getSerialDevices() {
        return serialDevicesHashMap;
    }

    /**
     * Gets all of the Serial Devices as Data Transfer Objects
     *
     * @return Arraylist containing all of the serial devices as Data Transfer Objects.
     */
    public synchronized static ArrayList<SerialDeviceDTO> getSerialDevicesAsDTOs() {

        if (serialDevicesHashMap.size() == 0) {
            return null;
        }

        ArrayList<SerialDeviceDTO> devices = new ArrayList<>();

        for (SerialDevice device : serialDevicesHashMap.values()) {
            SerialDeviceDTO dto = new SerialDeviceDTO(
                    new SerialPortPlaceHolder(device.getSerialPort().getSystemPortName(),
                            device.getSerialPort().getPortDescription(),
                            device.getSerialPort().getBaudRate()),
                    device.getId().getFriendlyID(),
                    device.getType().toString(),
                    device.running,
                    device.getDeviceLocation());
            devices.add(dto);
        }

        return devices;
    }

    /**
     * Adds a serial device to this Interaction Pi.
     *
     * @param serialDevice The DTO containing the information of the serial device to add.
     * @return Message acknowledging the success of the operation.
     * @throws IllegalArgumentException If the type of device submitted is not a valid one.
     */
    public synchronized String addSerialDevice(SerialDeviceDTO serialDevice) throws IllegalArgumentException{
        try {
            SerialPort selectedSerialPort = SerialPortsHandler.getSerialPort(serialDevice.getSerialPort().getSystemPortName());
            SerialDevice device;

            switch (DeviceType.valueOf(serialDevice.getType())) {
                case INTERACTION:
                    device = new SerialInteraction(selectedSerialPort);
                    break;
                case SENSOR:
                    device = new SerialSensor(selectedSerialPort);
                    break;
                default:
                    throw new IllegalArgumentException("The Type " + serialDevice.getType() + " is an invalid one");
            }

            serialDevicesHashMap.put(device.serialPort.getSystemPortName(), device);

            return "The new serial " + serialDevice.getType() + " has been successfully added.";

        } catch (IOException io) {
            io.printStackTrace();
            return "There was an issue trying to connect to the serial device " + serialDevice.getSerialPort().getSystemPortName();
        } catch (NoAvailableSerialDevices | NoSuchSerialDeviceException noAvailableSerialDevices) {
            return "The Serial Device " + serialDevice.getSerialPort().getSystemPortName() + " is no longer available";
        }
    }

    /**
     * Removes a Serial Device
     *
     * @param serialDeviceToRemove Data Transfer Object containing the information of the device to remove.
     * @return Message acknowledging the success of the operation.
     * @throws NoSuchSerialDeviceException When there is no serial device with the information in the DTO.
     */
    public String removeSerialDevice(SerialDeviceDTO serialDeviceToRemove) throws NoSuchSerialDeviceException {
        if (!serialDevicesHashMap.containsKey(serialDeviceToRemove.getSerialPort().getSystemPortName())) {
            throw new NoSuchSerialDeviceException("The Requested Serial Device " + serialDeviceToRemove.getSerialPort().getSystemPortName() + " does not exist.");
        } else {
            SerialDevice deviceToRemove = serialDevicesHashMap.get(serialDeviceToRemove.getSerialPort().getSystemPortName());
            if (deviceToRemove.getType() == DeviceType.INTERACTION) {
                SerialInteraction interaction = (SerialInteraction) deviceToRemove;
                if (interaction.getGameUID() != null) {
                    throw new IllegalStateException("The Selected Device is Taking Part in Game with ID " + interaction.getGameUID());
                }
            }
            serialDevicesHashMap.remove(serialDeviceToRemove.getSerialPort().getSystemPortName()).cleanup();
            return "The Serial Device has been successfully removed.";
        }
    }

    /**
     * Removes a Serial Device
     *
     * @param devicePortID Data Transfer Object containing the information of the device to remove.
     * @return Message acknowledging the success of the operation.
     * @throws NoSuchSerialDeviceException When there is no serial device with the information in the DTO.
     */
    public String removeSerialDevice(String devicePortID) throws NoSuchSerialDeviceException {
        if (!serialDevicesHashMap.containsKey(devicePortID)) {
            throw new NoSuchSerialDeviceException("The Requested Serial Device " + devicePortID + " does not exist.");
        } else {
            serialDevicesHashMap.remove(devicePortID).cleanup();
            return "Device " + devicePortID + " removed successfully.";
        }
    }

    /**
     * Gets a Serial Device given its port ID.
     *
     * @param serialPortID The port ID of the Serial Device
     * @return The Serial Device.
     * @throws NoSuchSerialDeviceException If there is not a Serial Device with the given Serial Port ID
     */
    public SerialDevice getSerialDevice(String serialPortID) throws NoSuchSerialDeviceException {
        if (serialDevicesHashMap.containsKey(serialPortID)) {
            return serialDevicesHashMap.get(serialPortID);
        } else {
            throw new NoSuchSerialDeviceException("There is no serial device with ID " + serialPortID);
        }
    }

    /**
     * Gets a Serial Device as Data Transfer Object.
     *
     * @param serialPortID The serial port ID of the Serial Device.
     * @return The Serial Device as a Data Transfer Object.
     */
    public SerialDeviceDTO getSerialDeviceAsDTO(String serialPortID) {

        if(serialDevicesHashMap.size() == 0 || serialDevicesHashMap.get(serialPortID) == null) {
            return null;
        }

        SerialDevice selectedDevice = serialDevicesHashMap.get(serialPortID);

        return new SerialDeviceDTO(new SerialPortPlaceHolder(selectedDevice.getSerialPort().getSystemPortName(),
                selectedDevice.getSerialPort().getPortDescription(),
                selectedDevice.getSerialPort().getBaudRate()),
                selectedDevice.getId().getFriendlyID(),
                selectedDevice.getType().toString(),
                selectedDevice.running,
                selectedDevice.getDeviceLocation());

    }

    /**
     * Gets all of the Serial Devices that are Interactions
     *
     * @return An ArrayList containing all of the Serial Devices that are Interactions
     */
    public ArrayList<SerialInteraction> getAllInteractions() {
        ArrayList<SerialInteraction> interactions = new ArrayList<>();
        for (SerialDevice device : serialDevicesHashMap.values()) {
            if (device instanceof SerialInteraction) {
                interactions.add((SerialInteraction)device);
            }
        }
        return interactions;
    }

    /**
     * Gets all of the Serial Devices that are Sensors
     *
     * @return An ArrayList containing all of the Serial Devices that are Sensors
     */
    public ArrayList<SerialSensor> getAllSensors() {
        ArrayList<SerialSensor> sensors = new ArrayList<>();
        for (SerialDevice device : serialDevicesHashMap.values()) {
            if (device instanceof SerialSensor) {
                sensors.add((SerialSensor)device);
            }
        }
        return sensors;
    }
    
}
