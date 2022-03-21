package pt.ulisboa.tecnico.interactionpiprocess.games;

import pt.ulisboa.tecnico.interactionpiprocess.exceptions.SingletonException;
import pt.ulisboa.tecnico.interactionpiprocess.interactions.Interaction;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialDevicesHandler;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialInteraction;
import pt.ulisboa.tecnico.rmi.StatusMessages;
import pt.ulisboa.tecnico.rmi.games.Games;
import pt.ulisboa.tecnico.rmi.interactionpi.InteractionResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class Game {

    private final static Logger LOGGER = java.util.logging.Logger.getLogger(Game.class.getName());

    private final Games gameName;
    private final String gameID;
    private final HashMap<String, Interaction> interactions;

    public Game(Games game, String ID) {
        this.gameName = game;
        this.gameID = ID;
        this.interactions = new HashMap<>();
    }

    /**
     * Readies all of the interactions in this game by reserving them to be used in this instance.
     *
     * @param selectedInteractions The interactions connected to this Interaction Pi that are to be used in this game instance
     * @return All the responses from each of the interactions acknowledging that they have been reserved.
     * @throws SingletonException If an exception is thrown while trying to get the SerialDevicesHandler instance.
     */
    public ArrayList<InteractionResponse> readyUp(ArrayList<String> selectedInteractions) throws SingletonException {
        ArrayList<InteractionResponse> interactionResponses = new ArrayList<>();
        ArrayList<SerialInteraction> interactions = SerialDevicesHandler.getInstance().getAllInteractions();

        if (selectedInteractions == null) {
            for (SerialInteraction interaction : interactions) {
                if (interaction.getGameUID() == null) {
                    interaction.setGameUID(gameID);
                    interaction.setGame(gameName);
                    this.addInteraction(interaction.getSerialPort().getSystemPortName(), interaction);
                    interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.OK, this.gameID, "Interaction Ready for " + this.gameName + " with ID " + this.gameID));
                }
            }
        } else {
            for (SerialInteraction interaction : interactions) {
                if (selectedInteractions.contains(interaction.getName()) && interaction.getGameUID() == null) {
                    interaction.setGameUID(gameID);
                    interaction.setGame(gameName);
                    this.addInteraction(interaction.getSerialPort().getSystemPortName(), interaction);
                    interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.OK, this.gameID, "Interaction Ready for " + this.gameName + " with ID " + this.gameID));
                }
            }
            if (interactionResponses.size() == 0) {
                throw new IllegalArgumentException("The Requested Interaction(s) are not available or do not exist.");
            }
        }
        return interactionResponses;
    }

    /**
     * Starts all of the interactions that will be used in this game.
     *
     * @return All the responses from each of the interactions containing their generated solutions
     */
    public ArrayList<InteractionResponse> start() {
        ArrayList<InteractionResponse> interactionResponses = new ArrayList<>();
        for (String interactionIdentifier : interactions.keySet()) {
            Interaction interaction = interactions.get(interactionIdentifier);
            try {
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.OK, this.gameID, interaction.start()));
            } catch (InterruptedException interruptedException) {
                LOGGER.warning("Interaction " + interaction.getName() + " Failed to Acknowledge START Command");
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.ERROR,  this.gameID,
                        "Interrupted Exception Occurred. Message: " + interruptedException.getMessage()));
            } catch (ExecutionException executionException) {
                LOGGER.warning("Interaction " + interaction.getName() + " Failed to Acknowledge START Command");
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.ERROR,  this.gameID,
                        "Execution Exception Occurred. Message: " + executionException.getMessage()));
            } catch (TimeoutException timeoutException) {
                LOGGER.warning("Interaction " + interaction.getName() + " Failed to Acknowledge START Command");
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.ERROR,  this.gameID,
                        "Operation Timed Out. Device Failed to Return an Answer. Message: " + timeoutException.getMessage()));
            } catch (IOException ioException) {
                LOGGER.warning("Interaction " + interaction.getName() + " Failed to Acknowledge START Command");
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.ERROR,  this.gameID,
                        "An Error Occurred While Trying to Write To Serial Port. Message: " + ioException.getMessage()));
            }
        }
        return interactionResponses;
    }

    /**
     * Pauses all of the interactions that are being used in this game.
     *
     * @return All the responses from each of the interactions acknowledging the operation
     */
    public ArrayList<InteractionResponse> pause() {
        ArrayList<InteractionResponse> interactionResponses = new ArrayList<>();
        for (String interactionIdentifier : interactions.keySet()) {
            Interaction interaction = interactions.get(interactionIdentifier);
            try {
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.OK, this.gameID, interaction.pause()));
            } catch (InterruptedException interruptedException) {
                LOGGER.warning("Interaction " + interaction.getName() + " Failed to Acknowledge PAUSE Command");
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.ERROR,  this.gameID,
                        "Interrupted Exception Occurred. Message: " + interruptedException.getMessage()));
            } catch (ExecutionException executionException) {
                LOGGER.warning("Interaction " + interaction.getName() + " Failed to Acknowledge PAUSE Command");
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.ERROR,  this.gameID,
                        "Execution Exception Occurred. Message: " + executionException.getMessage()));
            } catch (TimeoutException timeoutException) {
                LOGGER.warning("Interaction " + interaction.getName() + " Failed to Acknowledge PAUSE Command");
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.ERROR,  this.gameID,
                        "Operation Timed Out. Device Failed to Return an Answer. Message: " + timeoutException.getMessage()));
            } catch (IOException ioException) {
                LOGGER.warning("Interaction " + interaction.getName() + " Failed to Acknowledge PAUSE Command");
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.ERROR,  this.gameID,
                        "An Error Occurred While Trying to Write To Serial Port. Message: " + ioException.getMessage()));
            }
        }
        return interactionResponses;
    }

    /**
     * Stops all of the interactions that are being used in this game.
     *
     * @return All the responses from each of the interactions acknowledging the operation
     */
    public ArrayList<InteractionResponse> stop() {
        ArrayList<InteractionResponse> interactionResponses = new ArrayList<>();
        for (String interactionIdentifier : interactions.keySet()) {
            Interaction interaction = interactions.get(interactionIdentifier);
            interaction.setGameUID(null);
            interaction.setGame(null);
            LOGGER.info("Released Interaction " + interaction.getName());
            try {
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.OK, this.gameID, interaction.stop()));
            } catch (InterruptedException interruptedException) {
                LOGGER.warning("Interaction " + interaction.getName() + " Failed to Acknowledge STOP Command");
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.ERROR,  this.gameID,
                        "Interrupted Exception Occurred. Message: " + interruptedException.getMessage()));
            } catch (ExecutionException executionException) {
                LOGGER.warning("Interaction " + interaction.getName() + " Failed to Acknowledge STOP Command");
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.ERROR,  this.gameID,
                        "Execution Exception Occurred. Message: " + executionException.getMessage()));
            } catch (TimeoutException timeoutException) {
                LOGGER.warning("Interaction " + interaction.getName() + " Failed to Acknowledge STOP Command");
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.ERROR,  this.gameID,
                        "Operation Timed Out. Device Failed to Return an Answer. Message: " + timeoutException.getMessage()));
            } catch (IOException ioException) {
                LOGGER.warning("Interaction " + interaction.getName() + " Failed to Acknowledge STOP Command");
                interactionResponses.add(new InteractionResponse(interaction.getName(), StatusMessages.ERROR,  this.gameID,
                        "An Error Occurred While Trying to Write To Serial Port. Message: " + ioException.getMessage()));
            }
        }
        return interactionResponses;
    }

    /**
     * Adds interactions to this game instance.
     *
     * @param interactionIdentifier The unique identifier of the interaction
     * @param interaction The interaction corresponding to the identifier
     */
    public void addInteraction(String interactionIdentifier, Interaction interaction) {
        interactions.put(interactionIdentifier, interaction);
    }

    /**
     * Gets the name of the game
     *
     * @return The name of the game
     */
    public Games getGameName() {
        return gameName;
    }

    /**
     * Gets the unique ID of this game
     *
     * @return The unique game ID
     */
    public String getGameID() {
        return gameID;
    }

    /**
     * Gets all of the interactions that are being used in this instance.
     *
     * @return All of the interactions being used in this game instance.
     */
    public HashMap<String, Interaction> getInteractions() {
        return interactions;
    }

    /**
     * Gets an interaction that is being used in this instance.
     *
     * @param interactionID The ID of the interaction.
     * @return The Interaction.
     */
    public Interaction getInteraction(String interactionID) {return interactions.get(interactionID);}
}
