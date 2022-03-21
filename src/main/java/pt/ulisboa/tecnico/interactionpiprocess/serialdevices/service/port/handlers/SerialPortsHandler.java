package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.port.handlers;

import com.fazecast.jSerialComm.SerialPort;
import pt.ulisboa.tecnico.interactionpiprocess.exceptions.NoAvailableSerialDevices;
import pt.ulisboa.tecnico.interactionpiprocess.exceptions.NoSuchSerialDeviceException;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.dto.SerialPortPlaceHolder;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialDevicesHandler;

import java.util.ArrayList;
import java.util.Arrays;

public class SerialPortsHandler {

    public static ArrayList<SerialPort> getAllAvailableSerialPorts() throws NoAvailableSerialDevices {
        if (SerialPort.getCommPorts().length == 0) {
            throw new NoAvailableSerialDevices("There are no available serial devices.");
        }else {
            return new ArrayList<>(Arrays.asList(SerialPort.getCommPorts()));
        }
    }

    public synchronized static ArrayList<SerialPortPlaceHolder> getAvailableSerialPortsDTO() {
        ArrayList<SerialPortPlaceHolder> serialPorts = new ArrayList<>();
        try {
            for(SerialPort port : getAllAvailableSerialPorts()) {
                if (!SerialDevicesHandler.getSerialDevices().containsKey(port.getSystemPortName())) {
                    serialPorts.add(new SerialPortPlaceHolder(port.getSystemPortName(), port.getPortDescription(), port.getBaudRate()));
                }
            }
        } catch (NoAvailableSerialDevices ignored) { }
        return serialPorts;
    }

    public static SerialPort getSerialPort(String serialPortSystemName) throws NoSuchSerialDeviceException, NoAvailableSerialDevices {

        if (SerialPort.getCommPorts().length == 0) {
            throw new NoAvailableSerialDevices("There are no available serial devices");
        }

        for (SerialPort port : SerialPort.getCommPorts()) {
            if (port.getSystemPortName().equals(serialPortSystemName)) {
                return port;
            }
        }

        throw new NoSuchSerialDeviceException("There is no available serial device with name " + serialPortSystemName);
    }

}
