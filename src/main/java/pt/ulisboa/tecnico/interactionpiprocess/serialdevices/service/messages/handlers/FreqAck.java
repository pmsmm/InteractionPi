package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.handlers;

import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.SerialDeviceResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class FreqAck implements Callable<String> {

    private final ConcurrentHashMap<String, SerialDeviceResponse> map;
    private final String messageID;

    public FreqAck(ConcurrentHashMap<String, SerialDeviceResponse> q, String id) {
        this.map = q;
        this.messageID = id;
    }

    @Override
    public String call() {
        while (!map.containsKey(messageID)) {}
        String message = map.get(messageID).message;
        map.remove(messageID);
        return message;
    }
}
