package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.handlers;

import pt.ulisboa.tecnico.interactionpiprocess.api.MasterPi;
import pt.ulisboa.tecnico.interactionpiprocess.exceptions.NoSuchSerialDeviceException;
import pt.ulisboa.tecnico.interactionpiprocess.exceptions.SingletonException;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.DeviceType;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialDevice;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialDevicesHandler;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialInteraction;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.SerialDeviceResponse;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class PingThread implements Runnable{

    private static Logger LOGGER = null;
    private final ConcurrentHashMap<String, SerialDeviceResponse> responses;
    private final SerialDevice device;

    public PingThread(ConcurrentHashMap<String, SerialDeviceResponse> q, SerialDevice dev, Logger logger) {
        this.responses = q;
        this.device = dev;
        LOGGER = logger;
    }

    @Override
    public void run() {
        int numberOfFailedPings = 0;
        ExecutorService thread = Executors.newFixedThreadPool(1);
        while (numberOfFailedPings != 3) {
            try {
                Future<String> response = thread.submit(new PingCallable(device.ping(), responses));
                LOGGER.fine("Received " + response.get(5, TimeUnit.SECONDS));
                numberOfFailedPings = 0;
            } catch (IOException | InterruptedException | ExecutionException | TimeoutException exception) {
                LOGGER.warning(device.getSerialPort().getSystemPortName() + " failed to respond.\nFailed Pings: " + ++numberOfFailedPings);
            } finally {
                try {
                    if (numberOfFailedPings != 3) {
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            this.reportFailure();
            LOGGER.info("Serial Device has been removed from system since it's unreachable.");
        } catch (NoSuchSerialDeviceException | SingletonException | RemoteException noSuchSerialDevice) {
            LOGGER.warning("Caught exception in Ping Thread.");
        }
        LOGGER.info("Shutting Down Ping Thread");
    }

    private void reportFailure() throws SingletonException, NoSuchSerialDeviceException, RemoteException {
        if (device.getType() == DeviceType.INTERACTION) {
            SerialInteraction interaction = (SerialInteraction) device;
            if (interaction.getGame() != null && interaction.getGameUID() != null) {
                MasterPi.getInstance().getMasterPiProxy().reportDeviceFailure(interaction.getGame(), interaction.getGameUID(), interaction.getName());
            }
        } else {
            //TODO: This wont work for Sensors since they do not possess a Friendly ID;
            MasterPi.getInstance().getMasterPiProxy().reportDeviceFailure(device.getId().getFriendlyID());
        }
        SerialDevicesHandler.getInstance().removeSerialDevice(device.getSerialPort().getSystemPortName());
    }
}
