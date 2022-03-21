package pt.ulisboa.tecnico.interactionpiprocess.exceptions;

public class InvalidID extends Exception {
    public InvalidID(String message) {
        super(message);
    }

    public InvalidID(String message, Throwable err) {
        super(message, err);
    }
}
