package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.handlers;

import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialDevicesHandler;

public class SetupAck implements Runnable{

    private final String interactionID;
    private final String baudRate;
    private final String portName;

    public SetupAck(String portName, String interactionName, String baudRate) {
        this.interactionID = interactionName;
        this.baudRate = baudRate;
        this.portName = portName;
    }

    @Override
    public void run() {
        try{
            SerialDevicesHandler.getInstance().getSerialDevice(portName).setupDevice(interactionID, baudRate);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
