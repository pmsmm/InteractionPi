package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.handlers;

import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.SerialDeviceResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class StopAck implements Callable<String> {

    private final ConcurrentHashMap<String, SerialDeviceResponse> map;
    private final String messageID;

    public StopAck(ConcurrentHashMap<String, SerialDeviceResponse> responses, String s) {
        this.map = responses;
        this.messageID = s;
    }

    @Override
    public String call() {
        while (!map.containsKey(messageID)) {}
        String message = map.get(messageID).message;
        map.remove(messageID);
        return message;
    }
}
