package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.handlers;

import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.SerialDeviceResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class PingCallable implements Callable<String> {
    
    private final ConcurrentHashMap<String, SerialDeviceResponse> map;
    private final String ID;

    public PingCallable(String messageID, ConcurrentHashMap<String, SerialDeviceResponse> q) {
        this.ID = messageID;
        this.map = q;
    }

    @Override
    public String call() {
        while (!map.containsKey(ID)){
            
        }
        String message = map.get(ID).message;
        map.remove(ID);
        return message;
    }
}
