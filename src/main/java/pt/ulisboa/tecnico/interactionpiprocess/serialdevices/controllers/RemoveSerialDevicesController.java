package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.controllers;

import com.sun.istack.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pt.ulisboa.tecnico.interactionpiprocess.exceptions.NoSuchSerialDeviceException;
import pt.ulisboa.tecnico.interactionpiprocess.exceptions.SingletonException;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialDevicesHandler;

@Controller
@RequestMapping("/serialdevices/remove")
public class RemoveSerialDevicesController {

    @PostMapping(value = "/{serialDeviceID}")
    public @NotNull String removeSerialDeviceByID(@PathVariable("serialDeviceID")String serialDeviceID, Model model) {
        try {
            model.addAttribute("SuccessMessage", SerialDevicesHandler.getInstance().removeSerialDevice(serialDeviceID));
            return "Feedback Pages/Success";
        } catch (SingletonException | NoSuchSerialDeviceException exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

}
