package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.port.handlers;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class ComPortListener implements SerialPortDataListener {
    @Override
    public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }

    @Override
    public void serialEvent(SerialPortEvent event)
    {
        byte[] buffer = new byte[event.getSerialPort().bytesAvailable()];
        event.getSerialPort().readBytes(buffer, buffer.length);
        ReformatBuffer.parseByteArray(buffer, event.getSerialPort().getSystemPortName());
    }
}
