package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device;

import com.fazecast.jSerialComm.SerialPort;
import pt.ulisboa.tecnico.interactionpiprocess.games.GamesContainer;
import pt.ulisboa.tecnico.interactionpiprocess.interactions.Interaction;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.Commands;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.handlers.PauseAck;
import pt.ulisboa.tecnico.rmi.games.Games;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SerialInteraction extends SerialDevice implements Interaction {

    public static final DeviceType type = DeviceType.INTERACTION;
    private Games game;
    private String gameUID;
    private int points = 0;

    protected SerialInteraction(SerialPort serialPort) throws IOException {
        super(serialPort);
    }

    @Override
    public void setupDevice(String interactionName, String baudRate) {
        //TODO: When I have the setup complete I can proceed to create the respective directory to store this interaction data such as Logs, schematics, etc...
        try {
            if (!SETUP_COMPLETE) {
                this.id.setFriendlyID(interactionName);
                this.serialPort.setBaudRate(Integer.parseInt(baudRate));
                SETUP_COMPLETE = true;
                LOGGER.info("Setup of device at " + serialPort.getSystemPortName() + " is complete.");
                /*threadPool.submit(new PingThread(responses, this, LOGGER));
                LOGGER.info("Launching Ping Thread for " + serialPort.getSystemPortName());*/
            }
        } catch (NumberFormatException numberFormatException) {
            LOGGER.warning("The number " + baudRate + " is not a valid one. Serial Port Baud rate was kept at " + serialPort.getBaudRate());
        }
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public DeviceType getType() {
        return type;
    }

    @Override
    public String pause() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        LOGGER.info("Sending PAUSE command to " + serialPort.getSystemPortName());
        Future<String> response = threadPool.submit(new PauseAck(responses, writeToSerial(Commands.PAUSE)));
        return response.get(5, TimeUnit.SECONDS);
    }

    //TODO: Review and Test
    public final void acknowledgeInteractionSolved(String points) {
        try {
             GamesContainer.getInstance().creditPoints(gameUID, this.getName(), Integer.parseInt(points));
             LOGGER.info("Acknowledging INTERACTION_SOLVED message from " + this.getName());
             super.writeToSerial(Commands.INTERACTION_SOLVED_ACK);
        } catch (IOException exception) {
            LOGGER.severe("Exception Occurred while trying to Ack Interaction Solved");
            exception.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return this.id.getFriendlyID();
    }

    @Override
    public String getGameUID() {
        return gameUID;
    }

    @Override
    public void setGameUID(String gameUniqueID) {
        this.gameUID = gameUniqueID;
    }

    @Override
    public Games getGame() {
        return game;
    }

    @Override
    public void setGame(Games gameName) {
        this.game = gameName;
    }
}
