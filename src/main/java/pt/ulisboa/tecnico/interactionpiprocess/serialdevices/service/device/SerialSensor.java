package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device;

import com.fazecast.jSerialComm.SerialPort;
import pt.ulisboa.tecnico.interactionpiprocess.sensors.Sensor;
import pt.ulisboa.tecnico.interactionpiprocess.sensors.SensorInterface;
import pt.ulisboa.tecnico.interactionpiprocess.sensors.SensorType;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.Commands;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.handlers.FreqAck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SerialSensor extends SerialDevice implements SensorInterface {

    public final DeviceType type = DeviceType.SENSOR;
    public final HashMap<Short, Sensor> sensors = new HashMap<>();
    public String sensorLocation = "NOT DEFINED";

    public SerialSensor(SerialPort selectedSerialPort) throws IOException {
        super(selectedSerialPort);
    }

    @Override
    public void setupDevice(String SensorName, String frequency) {
        this.id.setFriendlyID(SensorName);
        this.serialPort.setBaudRate(Integer.parseInt(frequency));
        this.SETUP_COMPLETE = true;
        LOGGER.info(SensorName + " Setup at " + serialPort.getSystemPortName() + " is complete.");
    }

    @Override
    public void addSensor(short ioPin, int frequency) {
        LOGGER.info("Added Sensor at Pin " + ioPin + " with interval between readings of " + frequency);
        sensors.put(ioPin, new Sensor(ioPin, frequency));
    }

    @Override
    public String changeSensorFrequency(int ioPin, int newFrequency) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        if (newFrequency > 60000 || newFrequency < 1000) {
            throw new IllegalArgumentException("Frequency of Acquisition must be between 1000 and 60000.");
        }
        LOGGER.info("Sending FREQ command to " + serialPort.getSystemPortName());
        Future<String> response = threadPool.submit(new FreqAck(responses, writeToSerial(Commands.FREQ, newFrequency, ioPin)));
        return response.get(10, TimeUnit.SECONDS);
    }

    @Override
    public void reportReading(SensorType typeOfReading, double value, short ioPin) {
        sensors.get(ioPin).submitReading(typeOfReading, value);
    }

    public String getSensorLocation() {
        return this.sensorLocation;
    }

    public String setSensorLocation(String location) {
        this.sensorLocation = location;
        return "Sensor Location Set To " + location;
    }

    public ArrayList<Sensor> getAllSensors() {
        return new ArrayList<>(sensors.values());
    }

    @Override
    public DeviceType getType() {
        return type;
    }
}