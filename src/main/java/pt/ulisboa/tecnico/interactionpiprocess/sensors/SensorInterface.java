package pt.ulisboa.tecnico.interactionpiprocess.sensors;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface SensorInterface {
    void addSensor(short ioPin, int frequency);
    String changeSensorFrequency(int ioPin, int newFrequency) throws IOException, ExecutionException, InterruptedException, TimeoutException;
    void reportReading(SensorType typeOfReading, double value, short ioPin);
}