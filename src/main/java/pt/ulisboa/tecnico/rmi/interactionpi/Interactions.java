package pt.ulisboa.tecnico.rmi.interactionpi;

import java.io.Serializable;
import java.net.Inet4Address;
import java.util.ArrayList;

public class Interactions implements Serializable {

    private Inet4Address address;
    private int port;
    private ArrayList<String> interactionNames;

    public Interactions() {}

    public Interactions(ArrayList<String> interactionNames) { this.setInteractionNames(interactionNames); }

    public Inet4Address getAddress() {
        return address;
    }

    public void setAddress(Inet4Address address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ArrayList<String> getInteractionNames() {
        return interactionNames;
    }

    public void setInteractionNames(ArrayList<String> interactionNames) {
        this.interactionNames = interactionNames;
    }
}
