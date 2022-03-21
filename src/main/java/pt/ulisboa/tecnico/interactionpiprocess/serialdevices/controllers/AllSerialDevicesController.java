package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialDevicesHandler;

@Controller
@RequestMapping("/serialdevices/all")
public class AllSerialDevicesController {

    @GetMapping
    public String allSerialDevices(Model model) {
        model.addAttribute("SerialDevices", SerialDevicesHandler.getSerialDevicesAsDTOs());
        return "Serial Devices/All/AllSerialDevices";
    }

}
