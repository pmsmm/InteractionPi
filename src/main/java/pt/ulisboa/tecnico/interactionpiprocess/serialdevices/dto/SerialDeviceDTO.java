package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.dto;

public class SerialDeviceDTO {

    private SerialPortPlaceHolder serialPort;
    private String deviceName;
    private String type;
    private String deviceLocation;
    private boolean runningStatus;

    public SerialDeviceDTO(SerialPortPlaceHolder port, String friendlyName, String typeOfDevice, boolean status, String deviceLocation) {
        this.serialPort = port;
        this.deviceName = friendlyName;
        this.type = typeOfDevice;
        this.runningStatus = status;
        this.deviceLocation = deviceLocation;
    }

    public SerialDeviceDTO() {
        this.serialPort = new SerialPortPlaceHolder();
        this.deviceName = "";
        this.type = "";
        this.runningStatus = false;
    }

    public String getDeviceLocation() {
        return deviceLocation;
    }

    public void setDeviceLocation(String deviceLocation) {
        this.deviceLocation = deviceLocation;
    }

    public void setSerialPort(SerialPortPlaceHolder serialPort) {
        this.serialPort = serialPort;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRunningStatus(boolean runningStatus) {
        this.runningStatus = runningStatus;
    }

    public SerialPortPlaceHolder getSerialPort() {
        return serialPort;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public boolean isRunningStatus() {
        return runningStatus;
    }
}
