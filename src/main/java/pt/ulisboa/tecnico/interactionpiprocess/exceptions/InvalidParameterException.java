package pt.ulisboa.tecnico.interactionpiprocess.exceptions;

public class InvalidParameterException extends Exception {
    public InvalidParameterException(String message) {
        super(message);
    }

    public InvalidParameterException(String message, Throwable err) {
        super(message, err);
    }
}
