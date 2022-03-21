package pt.ulisboa.tecnico.interactionpiprocess.sensors;

import java.util.ArrayList;
import java.util.HashMap;

public class Sensor {

    private final HashMap<SensorType, ArrayList<Double>> readings;
    private final short ioPin;
    private final int frequency;

    public Sensor(short pin, int freq) {
        this.readings = new HashMap<>();
        this.ioPin = pin;
        this.frequency = freq;
    }

    /**
     * Submits a reading from a sensor and groups these readings by sensor type.
     *
     * @param type The type of sensor that submitted the reading.
     * @param value The value the sensor read.
     */
    public void submitReading(SensorType type, double value) {
        if (!readings.containsKey(type)) {
            readings.put(type, new ArrayList<>());
        }

        if(readings.get(type).size() < 10) {
            readings.get(type).add(value);
        } else {
            ArrayList<Double> sensorValues = readings.get(type);

            for(int i = 0; i < 1; i++){
                int j;

                for(j = 0; j < sensorValues.size()-1; j++){
                    sensorValues.set(j, sensorValues.get(j+1));
                }
                sensorValues.set(j, value);
            }
            readings.replace(type, sensorValues);
        }
    }

    /**
     * Get all of the readings from all of the sensor types
     *
     * @return The readings acquired.
     */
    public HashMap<SensorType, ArrayList<Double>> getReadings() {
        return readings;
    }

    /**
     * Get readings by sensor type.
     *
     * @param type The type of sensor to get the readings from.
     * @return The readings from that sensor type.
     */
    public ArrayList<Double> getReadings(SensorType type) {
        return readings.get(type);
    }


    public short getIoPin() {
        return ioPin;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getTypesOfReadings() {
        StringBuilder typesOfReadings = new StringBuilder();
        for (SensorType type : readings.keySet()) {
            typesOfReadings.append(type.toString()).append(" ");
        }
        return typesOfReadings.toString();
    }
}
