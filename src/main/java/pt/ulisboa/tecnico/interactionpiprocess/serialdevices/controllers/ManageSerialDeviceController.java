package pt.ulisboa.tecnico.interactionpiprocess.serialdevices.controllers;

import com.sun.istack.internal.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import pt.ulisboa.tecnico.interactionpiprocess.exceptions.*;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.dto.SerialSensorFrequencyDTO;
import pt.ulisboa.tecnico.interactionpiprocess.serialdevices.service.device.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Controller
@RequestMapping("/serialdevices/manage")
public class ManageSerialDeviceController {

    @GetMapping
    public @NotNull
    ModelAndView index() {
        return new ModelAndView("redirect:/serialdevices/all");
    }

    @GetMapping(value = {"/{id}"})
    public @NotNull String getSerialDeviceManagementPage(@PathVariable(value = "id") String id, Model model) {
        try {
            validateID(id);
            model.addAttribute("SerialDevice", SerialDevicesHandler.getInstance().getSerialDevice(id));
            return "Serial Devices/Manage/ManageSerialDevice";
        } catch (SingletonException | NoSuchSerialDeviceException | InvalidID e) {
            model.addAttribute("ErrorMessage", e.getMessage());
            return "Feedback Pages/Error";
        }

    }

    @GetMapping(value = {"/{id}/name"})
    public @NotNull String changeSerialDeviceName(@PathVariable(value = "id") String id, Model model) {
        try {
            validateID(id);
            model.addAttribute("CurrentName", SerialDevicesHandler.getInstance().getSerialDevice(id).getId().getFriendlyID());
            model.addAttribute("id", id);
            return "Serial Devices/Manage/Device Name/ManageDeviceName";
        } catch (SingletonException | InvalidID | NoSuchSerialDeviceException exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    //TODO: Alter this to a POST request since data is being changed or submitted to the server
    @GetMapping(value = {"/{id}/name/submit"})
    public @NotNull String changeSerialDeviceNameSubmit(@PathVariable(value = "id") String id,
                                               @RequestParam("value") Optional<String> newDeviceName,
                                               Model model) {
        try {
            validateID(id);
            if (!newDeviceName.isPresent()) {
                throw new InvalidParameterException("The new name for the device cannot be empty");
            }
            String deviceName = newDeviceName.get();
            if (deviceName.trim().equals("")) {
                throw new InvalidParameterException("The new device name cannot be empty");
            }

            SerialDevice device = SerialDevicesHandler.getInstance().getSerialDevice(id);
            device.getId().setFriendlyID(deviceName);

            model.addAttribute("SuccessMessage", "The Device's name was changed to " + deviceName);
            return "Feedback Pages/Success";

        } catch (SingletonException | InvalidParameterException | NoSuchSerialDeviceException | InvalidID exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Success";
        }
    }

    @GetMapping(value = {"/{id}/rate"})
    public @NotNull String changeSerialDeviceBaudRate(@PathVariable(value = "id") String id, Model model) {
        try {
            validateID(id);
            model.addAttribute("SerialPortBaudRate", SerialDevicesHandler.getInstance().getSerialDevice(id).getSerialPort().getBaudRate());
            model.addAttribute("id", id);
            return "Serial Devices/Manage/Device Rate/ManageDeviceBaudRate";
        } catch (SingletonException | NoSuchSerialDeviceException | InvalidID exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    //TODO: Change this to a POST Mapping in order to accomodate for the change of a value on the server
    @GetMapping(value = {"/{id}/rate/submit"})
    public @NotNull String changeSerialDeviceBaudRateSubmit(@PathVariable(value = "id") String id,
                                                   @RequestParam("value") Optional<Integer> newBaudRate,
                                                   Model model) {
        try {
            validateID(id);
            if (!newBaudRate.isPresent()) {
                throw new InvalidParameterException("The New Baud Rate cannot be empty");
            }
            if (newBaudRate.get() == 0) {
                throw new InvalidParameterException("The Baud Rate cannot be 0");
            }

            SerialDevice device = SerialDevicesHandler.getInstance().getSerialDevice(id);
            
            model.addAttribute("SuccessMessage", device.baud(newBaudRate.get()));
            return "Feedback Pages/Success";

        } catch (SingletonException | InvalidParameterException | NoSuchSerialDeviceException | InvalidID | InterruptedException
                | ExecutionException | TimeoutException | IOException exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    @GetMapping(value = {"/{id}/schematic"})
    public @NotNull String serialDeviceSchematic(@PathVariable(value = "id") String id, Model model) {
        try {
            validateID(id);
            model.addAttribute("SerialDevice", SerialDevicesHandler.getInstance().getSerialDevice(id));
            model.addAttribute("id", id);
            return "Serial Devices/Manage/Device Schematic/ManageDeviceSchematic";
        } catch (SingletonException | NoSuchSerialDeviceException | InvalidID exception) {
            model.addAttribute("Error", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    @PostMapping(value = {"/{id}/schematic/submit"})
    public @NotNull String changeSerialDeviceSchematic(@PathVariable(value = "id") String id,
                                                       @RequestParam("schematic") MultipartFile file,
                                                       Model model) {
        try {
            validateID(id);
            SerialDevicesHandler.getInstance().getSerialDevice(id).setSchematic(file);

            model.addAttribute("SuccessMessage", "The device's schematic was set successfully");
            return "Feedback Pages/Success";
        } catch (SingletonException | InvalidID | InvalidFileSpecificationException | NoSuchSerialDeviceException exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    //TODO: Review this method as the implementation does not seem correct
    @GetMapping(value = {"/{id}/schematic/view"}, produces = "application/pdf")
    public ResponseEntity<byte[]> serialDeviceSchematicView(@PathVariable(value = "id") String id) {
        try {
            validateID(id);
            SerialDevice device = SerialDevicesHandler.getInstance().getSerialDevice(id);

            if (device.getSchematic() == null) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                return new ResponseEntity<>("This device's schematic does not exist".getBytes(), headers, HttpStatus.BAD_REQUEST);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            String filename = device.getId().getFriendlyID() + "_schematic.pdf";

            headers.add("content-disposition", "inline;filename=" + filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            return new ResponseEntity<>(Files.readAllBytes(device.getSchematic().toPath()), headers, HttpStatus.OK);
        } catch (SingletonException | IOException | NoSuchSerialDeviceException | InvalidID singletonException) {
            singletonException.printStackTrace();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            return new ResponseEntity<>("An error occurred while trying to view the device's schematic".getBytes(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = {"/{id}/description"})
    public @NotNull String serialDeviceDescription(@PathVariable(value = "id") String id, Model model) {
        try {
            validateID(id);
            model.addAttribute("SerialDeviceDescription", SerialDevicesHandler.getInstance().getSerialDevice(id).getDescription());
            return "Serial Devices/Manage/Device Description/ManageDeviceDescription";
        } catch (SingletonException | InvalidID | NoSuchSerialDeviceException exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    @GetMapping(value = {"/{id}/description/submit"})
    public @NotNull String changeSerialDeviceDescription(@PathVariable(value = "id") String id,
                                                              @RequestParam("value") Optional<String> description,
                                                              Model model) {
        try {
            validateID(id);
            if (!description.isPresent() || description.get().trim().equals("")) {
                throw new InvalidParameterException("The new description cannot be empty");
            }
            SerialDevice device = SerialDevicesHandler.getInstance().getSerialDevice(id);
            device.setDescription(description.get());

            model.addAttribute("SuccessMessage", "The device's description was updated successfully");
            return "Feedback Pages/Success";
        } catch (InvalidParameterException | InvalidID | SingletonException | NoSuchSerialDeviceException exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    @GetMapping(value = {"/{id}/start"})
    public @NotNull String startSerialDevice(@PathVariable(value = "id") String id, Model model) {
        try {
            validateID(id);
            SerialDevice device = SerialDevicesHandler.getInstance().getSerialDevice(id);
            if (device.isRunning()) {
                model.addAttribute("SuccessMessage", "The serial device is already running");
            } else {
                model.addAttribute("SuccessMessage", device.start());
                device.setRunning(true);
            }
            return "Feedback Pages/Success";
        } catch (InvalidID | NoSuchSerialDeviceException | SingletonException |IOException | InterruptedException | TimeoutException | ExecutionException exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    @GetMapping(value = {"/{id}/stop"})
    public @NotNull String stopSerialDevice(@PathVariable(value = "id") String id,
                                            Model model) {
        try {
            validateID(id);
            SerialDevice device = SerialDevicesHandler.getInstance().getSerialDevice(id);

            if (!device.isRunning()) {
                model.addAttribute("SuccessMessage", "The serial device is already stopped");
            } else {
                model.addAttribute("SuccessMessage", device.stop());
                device.setRunning(false);
            }
            return "Feedback Pages/Success";
        } catch (InvalidID | NoSuchSerialDeviceException | SingletonException | IOException | InterruptedException | TimeoutException | ExecutionException exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    @GetMapping(value = {"/{id}/interactionpoints"})
    public @NotNull String serialInteractionPoints(@PathVariable(value = "id") String id,
                                                   Model model) {
        try {
            validateID(id);
            SerialDevice device = SerialDevicesHandler.getInstance().getSerialDevice(id);

            if (device.getType() != DeviceType.INTERACTION) {
                model.addAttribute("ErrorMessage", "The requested device with ID " + id + " is not an interaction");
                return "Feedback Pages/Error";
            } else {
                SerialInteraction interaction = (SerialInteraction) device;
                model.addAttribute("InteractionPoints", interaction.getPoints());
                return "Serial Devices/Manage/Interaction Points/ManageInteractionPoints";
            }

        } catch (InvalidID | NoSuchSerialDeviceException | SingletonException exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    //TODO: Change this to a POST request since there is a value being submitted to the server
    @GetMapping(value = {"/{id}/interactionpoints/submit"})
    public @NotNull String changeSerialInteractionPoints(@PathVariable(value = "id") String id,
                                                         @RequestParam("value") Optional<Integer> points,
                                                         Model model) {
        try {
            validateID(id);
            if (!points.isPresent()) {
                throw new InvalidParameterException("Please introduce a valid number of points for this interaction");
            }
            SerialDevice device = SerialDevicesHandler.getInstance().getSerialDevice(id);
            if (device.getType() != DeviceType.INTERACTION) {
                model.addAttribute("ErrorMessage", "The requested device with ID " + id + " is not an interaction");
                return "Feedback Pages/Error";
            } else {
                SerialInteraction interaction = (SerialInteraction) device;
                interaction.setPoints(points.get());
                model.addAttribute("SuccessMessage", "Interaction Points set to " + points.get());
                return "Feedback Pages/Success";
            }
        } catch (InvalidParameterException | InvalidID | SingletonException | NoSuchSerialDeviceException exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    @GetMapping(value = {"/{id}/sensorfrequency"})
    public String serialDeviceFrequency(@PathVariable(value = "id") String id,
                                        Model model) {
        try {
            validateID(id);
            SerialDevice device = SerialDevicesHandler.getInstance().getSerialDevice(id);

            if (device.getType() != DeviceType.SENSOR) {
                model.addAttribute("ErrorMessage", "The requested device with ID " + id + " is not a sensor");
                return "Feedback Pages/Error";
            } else {
                SerialSensor sensor = (SerialSensor) device;
                model.addAttribute("SerialSensor", sensor);
                model.addAttribute("FrequencyDTO", new SerialSensorFrequencyDTO());
                return "Serial Devices/Manage/Sensor Frequency/ManageSensorFrequency";
            }

        } catch (InvalidID | NoSuchSerialDeviceException | SingletonException exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    @PostMapping(value = {"/{id}/sensorfrequency/submit"})
    public @NotNull String setSerialDeviceFrequency(@PathVariable(value = "id") String id,
                                                    @ModelAttribute SerialSensorFrequencyDTO frequencyDTO,
                                                    Model model) {
        try {
            validateID(id);
            SerialDevice device = SerialDevicesHandler.getInstance().getSerialDevice(id);
            if (device.getType() != DeviceType.SENSOR) {
                model.addAttribute("ErrorMessage", "The requested device with ID " + id + " is not a sensor");
                return "Feedback Pages/Error";
            } else {
                model.addAttribute("SuccessMessage", ((SerialSensor)device).changeSensorFrequency(frequencyDTO.getPin(), frequencyDTO.getFrequency()));
                return "Feedback Pages/Success";
            }
        } catch (IllegalArgumentException | InterruptedException | TimeoutException | ExecutionException | IOException | NoSuchSerialDeviceException | SingletonException | InvalidID exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    @GetMapping(value = {"/{id}/location"})
    public String serialDeviceLocation(@PathVariable(value = "id") String id, Model model) {
        try {
            validateID(id);

            SerialDevice device = SerialDevicesHandler.getInstance().getSerialDevice(id);

            model.addAttribute("CurrentLocation", SerialDevicesHandler.getInstance().getSerialDevice(id).getDeviceLocation());
            model.addAttribute("id", id);

            return "Serial Devices/Manage/Device Location/ManageDeviceLocation";
        } catch (InvalidID | SingletonException | NoSuchSerialDeviceException exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    @PostMapping(value = {"/{id}/location/submit"})
    public String setSerialDeviceLocation(@PathVariable(value = "id") String id,
                                          @RequestParam("value") Optional<String> deviceLocation,
                                          Model model) {
        try {
            validateID(id);
            if (!deviceLocation.isPresent()) {
                throw new InvalidParameterException("Please introduce a valid number of points for this interaction");
            } else {
                SerialDevicesHandler.getInstance().getSerialDevice(id).setDeviceLocation(deviceLocation.get());
                model.addAttribute("SuccessMessage", "Device Location Set Successfully to " + deviceLocation.get());
                return "Feedback Pages/Success";
            }
        } catch (InvalidID | SingletonException | NoSuchSerialDeviceException | InvalidParameterException exception) {
            model.addAttribute("ErrorMessage", exception.getMessage());
            return "Feedback Pages/Error";
        }
    }

    private void validateID(String id) throws InvalidID {
        if (id == null) {
            throw new InvalidID("Invalid or Missing ID");
        } else if (id.trim().equals("")) {
            throw new InvalidID("ID cannot be empty");
        }
    }

}
