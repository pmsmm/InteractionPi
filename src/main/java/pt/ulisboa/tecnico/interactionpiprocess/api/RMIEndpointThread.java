package pt.ulisboa.tecnico.interactionpiprocess.api;

import pt.ulisboa.tecnico.rmi.interactionpi.InteractionPiProxy;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

//TODO: DELETE THREAD AFTER VERIFYING THAT IT IS WORKING WITHOUT IT

public class RMIEndpointThread implements Runnable{

    private static final Logger LOGGER = Logger.getLogger(RMIEndpointThread.class.getName());

    @Override
    public void run() {
        try {
            LOGGER.info("Setting up RMI server on port 8086");
            try(final DatagramSocket socket = new DatagramSocket()){
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                String ip = socket.getLocalAddress().getHostAddress();

                System.setProperty("java.rmi.server.hostname",ip);

                InteractionPiProxy server = new InteractionPiAPI();
                Registry registry = LocateRegistry.createRegistry(8086);
                registry.rebind("InteractionPiAPI", server);
            } catch(SocketException | UnknownHostException exception) {
                exception.printStackTrace();
            }

            while (true){}

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}