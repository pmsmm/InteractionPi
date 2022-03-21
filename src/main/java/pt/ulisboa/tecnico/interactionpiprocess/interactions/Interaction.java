package pt.ulisboa.tecnico.interactionpiprocess.interactions;

import pt.ulisboa.tecnico.rmi.games.Games;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface Interaction {
    String start() throws IOException, InterruptedException, ExecutionException, TimeoutException;
    String pause() throws IOException, InterruptedException, ExecutionException, TimeoutException;
    String stop() throws IOException, InterruptedException, ExecutionException, TimeoutException;
    String getName();
    String getGameUID();
    void setGameUID(String gameUniqueID);
    Games getGame();
    void setGame(Games game);
}
