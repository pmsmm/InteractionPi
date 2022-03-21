package pt.ulisboa.tecnico.interactionpiprocess.exceptions;

public class NoAvailableSerialDevices extends Exception {
    public NoAvailableSerialDevices(String message) {
        super(message);
    }

    public NoAvailableSerialDevices(String message, Throwable err) {
        super(message, err);
    }
}
