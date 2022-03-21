package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.dto;

public class SerialPortPlaceHolder {

    private String systemPortName;
    private String portDescription;
    private int baudRate;

    public SerialPortPlaceHolder(String systemPortName, String portDescription, int rate) {
        this.setSystemPortName(systemPortName);
        this.setPortDescription(portDescription);
        this.setBaudRate(rate);
    }

    public SerialPortPlaceHolder() {}

    public String getSystemPortName() {
        return systemPortName;
    }

    public String getPortDescription() {
        return portDescription;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setSystemPortName(String systemPortName) {
        this.systemPortName = systemPortName;
    }

    public void setPortDescription(String portDescription) {
        this.portDescription = portDescription;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }
}
