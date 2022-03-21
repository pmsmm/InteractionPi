package pt.ulisboa.tecnico.interactionpiprocess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pt.ulisboa.tecnico.interactionpiprocess.api.InteractionPiAPI;
import pt.ulisboa.tecnico.rmi.interactionpi.InteractionPiProxy;

import java.io.File;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class InteractionPiProcessApplication {

    private static final Logger LOGGER = Logger.getLogger(InteractionPiProcessApplication.class.getName());
    //private static final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    public InteractionPiProcessApplication() {

        String APP_DIRECTORY = System.getProperty("user.home") + File.separator + "Peddy_Room_System"
                + File.separator + "InteractionPiProcess" + File.separator;

        File directory = new File(APP_DIRECTORY);
        LOGGER.log(Level.INFO, "Attempting to create application directory...");
        if (directory.mkdirs()) {
            LOGGER.log(Level.INFO, "Directory" + APP_DIRECTORY + "created successfully!");
        } else {
            LOGGER.log(Level.INFO, "Directory " + APP_DIRECTORY + " already exists.");
        }
        
        //threadPool.submit(new RMIEndpointThread());
        short PORT = 8086;

        LOGGER.info("Setting up RMI server on port " + PORT);
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String ip = socket.getLocalAddress().getHostAddress();

            System.setProperty("java.rmi.server.hostname",ip);

            InteractionPiProxy server = new InteractionPiAPI();
            Registry registry = LocateRegistry.createRegistry(PORT);
            registry.rebind("InteractionPiAPI", server);
        } catch(SocketException | UnknownHostException | RemoteException exception) {
            exception.printStackTrace();
        }

    }

    public static void main(String[] args) {
        SpringApplication.run(InteractionPiProcessApplication.class, args);
    }

}
