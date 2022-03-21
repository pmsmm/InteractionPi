package pt.ulisboa.tecnico.interactionpiprocess;

import com.fazecast.jSerialComm.SerialPort;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pt.ulisboa.tecnico.interactionpiprocess.exceptions.SingletonException;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.dto.SerialDeviceDTO;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.dto.SerialPortPlaceHolder;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialDevicesHandler;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialInteraction;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.messages.Commands;

import java.io.IOException;

public class SerialDeviceClassTests {

    private static SerialInteraction interaction;

    @BeforeAll
    static void setup() {
        try {
            SerialPort port = SerialPort.getCommPorts()[0];
            SerialDevicesHandler.getInstance().addSerialDevice(new SerialDeviceDTO(
                    new SerialPortPlaceHolder(
                            port.getSystemPortName(), port.getPortDescription(), 9600), "TestName", "INTERACTION", false, "Somewhere"));
            interaction = SerialDevicesHandler.getInstance().getAllInteractions().get(0);
        } catch (SingletonException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void writeToSerialMethod() {
        try {
            interaction.writeToSerial(Commands.BAUD, 9600);
            interaction.writeToSerial(Commands.START);
            interaction.writeToSerial(Commands.BAUD, 5000, 14);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
