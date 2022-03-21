package pt.ulisboa.tecnico.interactionpiprocess.exceptions;

public class NoSuchSerialDeviceException extends Exception {
    public NoSuchSerialDeviceException(String message) {
        super(message);
    }

    public NoSuchSerialDeviceException(String message, Throwable err) {
        super(message, err);
    }
}
