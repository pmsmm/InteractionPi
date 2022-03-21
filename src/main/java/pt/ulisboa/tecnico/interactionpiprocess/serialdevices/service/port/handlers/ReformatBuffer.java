package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.port.handlers;

import pt.ulisboa.tecnico.interactionpiprocess.exceptions.NoSuchSerialDeviceException;
import pt.ulisboa.tecnico.interactionpiprocess.exceptions.SingletonException;
import pt.ulisboa.tecnico.interactionpiprocess.sensors.SensorType;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialDevicesHandler;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialInteraction;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialSensor;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.SerialDeviceResponse;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.handlers.SetupAck;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ReformatBuffer {

    private static final Logger LOGGER = Logger.getLogger(ReformatBuffer.class.getName());

    private static final String SETUP = "SETUP";
    private static final String START_ACK = "START_ACK";
    private static final String PAUSE_ACK = "PAUSE_ACK";
    private static final String INTERACTION_SOLVED = "INTERACTION_SOLVED";
    private static final String STOP_ACK = "STOP_ACK";
    private static final String PING = "PING";
    private static final String SENSOR = "SENSOR";
    private static final String READING = "READING";
    private static final String FREQ_ACK = "FREQ_ACK";

    static int cutoffASCII = 10; // ASCII code of the character used for cut-off between received messages
    static String bufferReadToString = ""; // empty, but not null

    public static synchronized void parseByteArray(byte[] readBuffer, String portName) {

        String stringFromByteArray = new String(readBuffer);
        bufferReadToString = bufferReadToString.concat(stringFromByteArray);

        if((bufferReadToString.indexOf(cutoffASCII) + 1) > 0) {
            String outputString = bufferReadToString.substring(0, bufferReadToString.indexOf(cutoffASCII) + 1);
            bufferReadToString = bufferReadToString.substring(bufferReadToString.indexOf(cutoffASCII) + 1); // adjust as needed to accommodate the CRLF convention ("\n\r"), ASCII 10 & 13

            processMessageInteraction(portName, outputString);
        }
    }

    private static void processMessageInteraction(String portName, String messageFromSerialDevice) {

        HashMap<String, String> commandValuePair = new HashMap<>();
        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        for(String token : messageFromSerialDevice.split(";")){
            String[] tokens = token.split(":");
            commandValuePair.put(tokens[0], tokens[1].replace("\r\n", ""));
        }

        try {
            SerialDevicesHandler handler = SerialDevicesHandler.getInstance();

            //TODO: Do not forget the ID parameter
            switch (commandValuePair.get("COM")){
                case SETUP:
                    threadPool.execute(new SetupAck(portName, commandValuePair.get("INT_NAME"), commandValuePair.get("BAUD")));
                    break;
                case STOP_ACK:
                case PAUSE_ACK:
                case START_ACK:
                case FREQ_ACK:
                case PING:
                    handler.getSerialDevice(portName).updateResponseQueue(commandValuePair.get("ID"), new SerialDeviceResponse(commandValuePair.get("MSG")));
                    break;
                case INTERACTION_SOLVED:
                    ((SerialInteraction)handler.getSerialDevice(portName)).acknowledgeInteractionSolved(commandValuePair.get("PNT"));
                    break;
                case SENSOR:
                    ((SerialSensor)handler.getSerialDevice(portName)).addSensor((short)Integer.parseInt(commandValuePair.get("PIN")),
                            Integer.parseInt(commandValuePair.get("FREQUENCY")));
                    break;
                case READING:
                    ((SerialSensor)handler.getSerialDevice(portName)).reportReading(SensorType.valueOf(commandValuePair.get("TYPE")),
                            Double.parseDouble(commandValuePair.get("VALUE")), (short)Integer.parseInt(commandValuePair.get("PIN")));
                    break;
            }

        } catch (SingletonException | NoSuchSerialDeviceException exception) {
            LOGGER.severe("An error occurred while processing a message from port " + portName);
            exception.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
