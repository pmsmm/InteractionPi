package pt.ulisboa.tecnico.interactionpiprocess.exceptions;

public class InvalidFileSpecificationException extends Exception{
    public InvalidFileSpecificationException(String message) {
        super(message);
    }

    public InvalidFileSpecificationException(String message, Throwable err) {
        super(message, err);
    }
}
