package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device;

/**
 * Identifies an interaction not only by its unique ID (The port ID) but also by its friendly name that usually symbolizes
 * what this interaction is about.
 *
 * @author Pedro Maria
 * @version 1.0
 * @since 1.0
 */

public final class Identification {

    private final String portID;
    private String friendlyID;

    public Identification() {
        friendlyID = "";
        portID = "";
    }

    public Identification(String friendlyDeviceName, String portID) {
        this.friendlyID = friendlyDeviceName;
        this.portID = portID;
    }

    public String getFriendlyID() {
        return friendlyID;
    }

    public void setFriendlyID(String friendlyID) {
        this.friendlyID = friendlyID;
    }

    public String getPortID() {
        return portID;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Identification)) {
            return false;
        }

        Identification idToCompare = (Identification) obj;

        return friendlyID.equals(idToCompare.getFriendlyID()) && portID.equals(idToCompare.getPortID());

    }

    @Override
    public String toString() {
        return "ID: " + friendlyID + "\n Port ID: " + portID;
    }

}
