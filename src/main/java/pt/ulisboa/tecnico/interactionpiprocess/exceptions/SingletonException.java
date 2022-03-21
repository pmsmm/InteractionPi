package pt.ulisboa.tecnico.interactionpiprocess.exceptions;

public class SingletonException extends Exception {
    public SingletonException(String errorMessage) {
        super(errorMessage);
    }

    public SingletonException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    public SingletonException(){
        super("An instance of this Singleton class already exists. Please do not try to instantiate another object as" +
                "this could lead to unexpected behaviour.");
    }
}
