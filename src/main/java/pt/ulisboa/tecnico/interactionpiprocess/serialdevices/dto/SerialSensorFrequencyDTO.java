package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.dto;

public class SerialSensorFrequencyDTO {

    private int pin;
    private int frequency;

    public SerialSensorFrequencyDTO() {}

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
