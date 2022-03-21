package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.controllers;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import pt.ulisboa.tecnico.interactionpiprocess.exceptions.SingletonException;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.dto.SerialDeviceDTO;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.DeviceTypes;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.SerialDevicesHandler;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.port.handlers.SerialPortsHandler;

@Controller
@RequestMapping(value = "/serialdevices/add")
public class AddSerialDevicesController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("SerialDevices", SerialPortsHandler.getAvailableSerialPortsDTO());
        model.addAttribute("SerialDeviceDTO", new SerialDeviceDTO());
        return "Serial Devices/Add/AddSerialDevice";
    }

    @PostMapping(value = "/submit")
    public String submit(@ModelAttribute SerialDeviceDTO dto, Model model) {

        String resultOfValidation = validateSerialDeviceDTO(dto);
        if (resultOfValidation != null) {
            model.addAttribute("ErrorMessage", resultOfValidation);
            return "Feedback Pages/Error";
        }

        try {
            model.addAttribute("SuccessMessage", SerialDevicesHandler.getInstance().addSerialDevice(dto));
            return "Feedback Pages/Success";
        } catch (SingletonException | IllegalArgumentException exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }

    }

    @PostMapping(value = "/cancel")
    public ModelAndView cancel() {
        return new ModelAndView("redirect:/serialdevices");
    }

    private @Nullable String validateSerialDeviceDTO(SerialDeviceDTO dataTransferObject) {
        if (dataTransferObject == null) {
            return "Invalid Serial Device.";
        }

        if (dataTransferObject.getSerialPort().getSystemPortName().trim().equals("") || dataTransferObject.getSerialPort().getSystemPortName() == null) {
            return "The Port ID cannot be empty or null";
        }

        if (dataTransferObject.getSerialPort().getPortDescription().trim().equals("") || dataTransferObject.getSerialPort().getPortDescription() == null) {
            return "The Port Description cannot be empty or null";
        }

        if (dataTransferObject.getSerialPort().getBaudRate() == 0) {
            return "The Baud Rate cannot be 0";
        }
        
        if (!DeviceTypes.getDeviceTypesAsStrings().contains(dataTransferObject.getType())) {
            return "The type of device is not a valid one.";
        }
        
        return null;
    }
}
