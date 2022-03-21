package pt.ulisboa.tecnico.interactionpiprocess.games;

import pt.ulisboa.tecnico.interactionpiprocess.api.MasterPi;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialDevicesHandler;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialInteraction;
import pt.ulisboa.tecnico.rmi.games.Games;
import pt.ulisboa.tecnico.rmi.interactionpi.InteractionPiResponse;
import pt.ulisboa.tecnico.rmi.masterpi.MasterPiProxy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class GamesContainer {

    private static final Logger LOGGER = Logger.getLogger(GamesContainer.class.getName());

    private static GamesContainer instance;
    private static HashMap<String, Game> gamesReady;
    private static HashMap<String, Game> gamesInExecution;

    private GamesContainer() {
        gamesReady = new HashMap<>();
        gamesInExecution = new HashMap<>();
    }

    public static GamesContainer getInstance() {
        if (instance == null) {
            instance = new GamesContainer();
        }
        return instance;
    }

    /**
     * Gets all of the games currently running in this Interaction Pi.
     *
     * @return The games currently running.
     */
    public static HashMap<String, Game> getGamesInExecution() {
        return gamesInExecution;
    }

    /**
     * Creates a new game instance
     *
     * @param gameName The name of the game to be created
     * @param gameUID The unique ID of the game instance determined by the Master Pi
     * @param selectedInteractions The interactions to be used in this instance as selected by the user in the Master Pi
     * @return The response acknowledging the operation
     * @throws Exception If an error occurs during the ready up phase of the instance creation.
     */
    public synchronized InteractionPiResponse createNewGame(Games gameName, String gameUID, ArrayList<String> selectedInteractions) throws Exception {
        Game game = new Game(gameName, gameUID);

        InteractionPiResponse response = new InteractionPiResponse(gameUID, gameName);
        response.setInteractions(game.readyUp(selectedInteractions));

        gamesReady.put(gameUID, game);

        return response;
    }

    /**
     * Starts a game instance.
     *
     * @param gameName The name of the game to start.
     * @param gameUID The unique ID of the game to start.
     * @return The response acknowledging the operation.
     */
    public synchronized InteractionPiResponse startGame(Games gameName, String gameUID) {
        Game gameToExecute = gamesReady.get(gameUID);

        InteractionPiResponse response = new InteractionPiResponse(gameUID, gameName);
        response.setInteractions(gameToExecute.start());

        gamesInExecution.put(gameUID, gameToExecute);
        gamesReady.remove(gameUID);

        return response;
    }

    /**
     * Pauses a game instance.
     *
     * @param gameName The name of the game to pause.
     * @param gameUID The unique ID of the game to pause.
     * @return The response acknowledging the operation.
     */
    public synchronized InteractionPiResponse pauseGame(Games gameName, String gameUID) {
        InteractionPiResponse response = new InteractionPiResponse(gameUID, gameName);
        response.setInteractions(gamesInExecution.get(gameUID).pause());
        return response;
    }

    /**
     * Stops a game instance.
     *
     * @param gameName The name of the game to stop.
     * @param gameUID The unique ID of the game to stop.
     * @return The response acknowledging the operation.
     */
    public synchronized InteractionPiResponse stopGame(Games gameName, String gameUID) {
        InteractionPiResponse response = new InteractionPiResponse(gameUID, gameName);
        if (gamesInExecution.containsKey(gameUID)) {
            response.setInteractions(gamesInExecution.remove(gameUID).stop());
        }
        return response;
    }

    //##################################################################################################################

    /**
     * Deletes a game instance.
     *
     * @param gameUID The unique ID of the instance to delete.
     * @return Message acknowledging the deletion.
     */
    public String deleteGame(String gameUID) {
        if (gamesReady.containsKey(gameUID)) {
            gamesReady.remove(gameUID);
            LOGGER.info("Deleted Game with ID: " + gameUID);
            return "The Game with ID: " + gameUID + " was Removed Successfully";
        }
        throw new IllegalArgumentException("No such game with ID " + gameUID);
    }

    /**
     * Lets the Master Pi know that an interaction has been solved and what points does this credit to the group/user that
     * solved the interaction. In the case of standalone execution it simply issues a message stating that the interaction
     * has been solved.
     *
     * @param gameUID The game ID where this interaction is being used.
     * @param interactionFriendlyID The name of the interaction whose points should be credited to.
     * @param points The number of points to be credited.
     */
    public synchronized void creditPoints(String gameUID, String interactionFriendlyID, int points) {
        try {
            if (gameUID != null) {
                MasterPiProxy masterProxy = MasterPi.getInstance().getMasterPiProxy();
                Executors.newFixedThreadPool(2).submit(() -> {
                    try {
                        masterProxy.setAcquiredPoints(gamesInExecution.get(gameUID).getGameName(), gameUID, interactionFriendlyID, points);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                LOGGER.info("Interaction " + interactionFriendlyID + " has been solved.");
                for (SerialInteraction interaction : SerialDevicesHandler.getInstance().getAllInteractions()) {
                    if (interaction.getName().equals(interactionFriendlyID)) {
                        interaction.setRunning(false);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
