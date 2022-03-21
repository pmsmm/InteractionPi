package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/serialdevices")
public class SerialDevicesController {

    @GetMapping
    public String serialDevicesHomePage() {
        return "Serial Devices/SerialDevices";
    }

}