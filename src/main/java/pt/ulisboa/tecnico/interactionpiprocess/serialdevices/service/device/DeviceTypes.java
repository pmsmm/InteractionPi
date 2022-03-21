package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device;

import java.util.ArrayList;

/**
 * Auxiliary class that returns the device types as an arraylist to be used in validation of user input.
 *
 * @author Pedro Maria
 * @version 1.0
 * @since 1.0
 */

public class DeviceTypes {

    public static ArrayList<String> getDeviceTypesAsStrings() {
        ArrayList<String> deviceTypesAsStrings = new ArrayList<>();
        for (DeviceType type : DeviceType.values()) {
            deviceTypesAsStrings.add(type.toString());
        }
        return deviceTypesAsStrings;
    }

}
