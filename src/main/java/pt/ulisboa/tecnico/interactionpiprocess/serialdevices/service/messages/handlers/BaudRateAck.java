package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.handlers;

import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.SerialDeviceResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class BaudRateAck implements Callable<String> {

    private final ConcurrentHashMap<String, SerialDeviceResponse> responses;
    private final String ID;

    public BaudRateAck(ConcurrentHashMap<String, SerialDeviceResponse> q, String messageID) {
        this.responses = q;
        this.ID = messageID;
    }

    @Override
    public String call() throws Exception {
        while (!responses.containsKey(ID)) {}
        String message = responses.get(ID).message;
        responses.remove(ID);
        return message;
    }
}
