package pt.ulisboa.tecnico.interactionpiprocess.api;

import pt.ulisboa.tecnico.rmi.masterpi.MasterPiProxy;

import java.net.Inet4Address;
import java.rmi.RemoteException;
import java.util.logging.Logger;

public class MasterPi {

    private final static Logger LOGGER = Logger.getLogger(MasterPi.class.getName());
    private static MasterPiProxy masterPiProxy;
    private static MasterPi instance;
    private static Inet4Address masterPiAddress;

    private MasterPi() {}

    public static MasterPi getInstance() {
        if (instance == null) {
            instance = new MasterPi();
        }
        return instance;
    }

    public void setMasterPiProxy(MasterPiProxy masterPiProxy) {
        try {
            MasterPi.masterPiProxy = masterPiProxy;
            LOGGER.info(masterPiProxy.test());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public MasterPiProxy getMasterPiProxy() {
        return masterPiProxy;
    }

    public Inet4Address getMasterPiAddress() {
        return masterPiAddress;
    }

    public void setMasterPiAddress(Inet4Address address) {
        masterPiAddress = address;
    }
}
