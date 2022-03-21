package pt.ulisboa.tecnico.interactionpiprocess.api;

import pt.ulisboa.tecnico.interactionpiprocess.exceptions.SingletonException;
import pt.ulisboa.tecnico.interactionpiprocess.games.Game;
import pt.ulisboa.tecnico.interactionpiprocess.games.GamesContainer;
import pt.ulisboa.tecnico.interactionpiprocess.interactions.Interaction;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialDevicesHandler;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialInteraction;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.Commands;
import pt.ulisboa.tecnico.rmi.games.GameCommands;
import pt.ulisboa.tecnico.rmi.games.Games;
import pt.ulisboa.tecnico.rmi.interactionpi.InteractionPiProxy;
import pt.ulisboa.tecnico.rmi.interactionpi.InteractionPiResponse;
import pt.ulisboa.tecnico.rmi.interactionpi.Interactions;
import pt.ulisboa.tecnico.rmi.masterpi.MasterPiProxy;

import java.io.IOException;
import java.net.Inet4Address;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class InteractionPiAPI extends UnicastRemoteObject implements InteractionPiProxy {

    public static String machineLocation = "Location Not Set";
    
    public InteractionPiAPI() throws RemoteException {}

    /**
     * The method called to handle the basic functions of a game like the Start, Stop, Ready and Pause ones.
     *
     * @param game The enum containing the name of the game.
     * @param gameID The unique ID of the instance associated to the commands.
     * @param command The actual command to be sent to the interaction pies like START, STOP, etc..
     * @param selectedInteractions Only used for the Ready command to reserve the interactions that are to be used in
     *                             this game instance.
     * @return The object containing the responses from all of the interaction pies to which the request was sent
     * @throws Exception If an invalid command is supplied.
     */
    @Override
    public InteractionPiResponse gameRequestsHandler(Games game, String gameID, GameCommands command, ArrayList<String> selectedInteractions) throws Exception {
        switch (command) {
            case READY:
                return GamesContainer.getInstance().createNewGame(game, gameID, selectedInteractions);
            case START:
                return GamesContainer.getInstance().startGame(game, gameID);
            case PAUSE:
                return GamesContainer.getInstance().pauseGame(game, gameID);
            case STOP:
                return GamesContainer.getInstance().stopGame(game, gameID);
            default:
                throw new IllegalArgumentException("Invalid Command");
        }
    }

    /**
     * Deletes the game with the given ID.
     *
     * @param gameID The ID of the game to delete.
     * @return Message acknowledging the operation.
     * @throws RemoteException If there is no game with the given ID.
     */
    @Override
    public String deleteGame(String gameID) throws RemoteException {
        return GamesContainer.getInstance().deleteGame(gameID);
    }

    /**
     * Invoked by the Master Pi to check if a response is returned and therefore the Interaction Pi is running
     *
     * @return Message acknowledging that the Interaction Pi is working
     * @throws RemoteException RMI problems
     */
    @Override
    public String ping() throws RemoteException{
        return "Hi from Interaction Pi";
    }

    /**
     * Sets up the connection between the Interaction Pi and the Master Pi. Invoked by the Master Pi after the connection
     * to the interaction Pi has been set so that the latter is able to communicate with the former.
     *
     * @param IpAddress The IPV4 address of the Master Pi
     * @return Message acknowledging the operation
     * @throws RemoteException If anything fails during the establishment of this connection
     */
    @Override
    public String setupBidirectionalLink(Inet4Address IpAddress) throws RemoteException {
        try {
            MasterPi.getInstance().setMasterPiProxy((MasterPiProxy) LocateRegistry.getRegistry(IpAddress.getHostAddress(), 8087).lookup("MasterPiAPI"));
            MasterPi.getInstance().setMasterPiAddress(IpAddress);
            return "Bidirectional Link established successfully";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets all of the Interaction names that are connected to this interaction Pi, even if they are being used in another
     * game.
     *
     * @return All of the interaction names connected to this Interaction Pi
     * @throws RemoteException If anything fails during the acquisition of the interaction names
     */
    @Override
    public Interactions getAllInteractions() throws RemoteException {
        try {
            ArrayList<String> interactionNames = new ArrayList<>();
            for (Interaction interaction : SerialDevicesHandler.getInstance().getAllInteractions()) {
                interactionNames.add(interaction.getName());
            }

            return new Interactions(interactionNames);
        } catch (SingletonException e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * Gets all of the available Interaction names that are connected to this interaction Pi.
     *
     * @return All of the interaction names connected to this Interaction Pi that are available to participate in a game
     * @throws RemoteException If anything fails during the acquisition of the interaction names
     */
    @Override
    public Interactions getAllAvailableInteractions() throws RemoteException {
        try {
            ArrayList<String> interactionNames = new ArrayList<>();
            for (Interaction interaction : SerialDevicesHandler.getInstance().getAllInteractions()) {
                if (interaction.getGameUID() == null) {
                    interactionNames.add(interaction.getName());
                }
            }

            return new Interactions(interactionNames);
        } catch (SingletonException e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public String setInteractionPiLocation(String location) throws RemoteException {
        if (location == null || location.trim().equals("")) {
            throw new RemoteException("Location cannot be null or empty. Keeping previous location.");
        } else {
            machineLocation = location;
            return "Location set to " + location;
        }
    }

    @Override
    public String getInteractionPiLocation() throws RemoteException {
        return machineLocation;
    }

    @Override
    public String setInteractionSolved(Games game, String gameID, String interactionName) throws RemoteException {
        Game gameInExecution = GamesContainer.getGamesInExecution().get(gameID);
        try {
            for (Interaction serialInteraction : gameInExecution.getInteractions().values()) {
                SerialInteraction temp = (SerialInteraction) serialInteraction;
                if (temp.getName().equals(interactionName)) {
                    return temp.writeToSerial(Commands.INTERACTION_SOLVED);
                }
            }
            throw new RemoteException("Interaction with ID " + interactionName + "not found");
        } catch (IOException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public String sendMessageToInteraction(Games game, String gameID, String interactionName, String message) throws RemoteException {
        Game gameInExecution = GamesContainer.getGamesInExecution().get(gameID);
        try {
            for (Interaction serialInteraction : gameInExecution.getInteractions().values()) {
                SerialInteraction temp = (SerialInteraction) serialInteraction;
                if (temp.getName().equals(interactionName)) {
                    return temp.writeToSerial(Commands.MSG, message);
                }
            }
            throw new RemoteException("Interaction with ID " + interactionName + "not found");
        } catch (IOException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public String startAllSerialInteractions() throws RemoteException {
        return null;
    }

    @Override
    public String startAllSerialSensors() throws RemoteException {
        return null;
    }

    @Override
    public String startAllDevices() throws RemoteException {
        return null;
    }
}
