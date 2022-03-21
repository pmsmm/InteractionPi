package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.handlers;

import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialDevice;

import java.io.IOException;

public class Setup implements Runnable{

    private final SerialDevice device;

    public Setup(SerialDevice serialDevice) {
        device = serialDevice;
    }

    @Override
    public void run() {
        while (!device.SETUP_COMPLETE) {
            try {
                device.setup();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    //TODO: Check how can I eliminate this Busy Waiting warning
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
