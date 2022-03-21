package pt.ulisboa.tecnico.interactionpiprocess.homepage.management;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pt.ulisboa.tecnico.interactionpiprocess.api.InteractionPiAPI;

import java.rmi.RemoteException;
import java.util.Optional;

@Controller
@RequestMapping("/interactionpi/manage")
public class InteractionPiManagementController {

    @GetMapping("/location")
    public String getLocationManagementPage(Model model) {
        model.addAttribute("CurrentLocation", InteractionPiAPI.machineLocation);
        return "InteractionPi/ManageInteractionPiLocation";
    }

    @PostMapping("/location/submit")
    public String getLocationManagementPage(@RequestParam("value") Optional<String> deviceLocation, Model model) {
        try {
            if(!deviceLocation.isPresent() || deviceLocation.get().trim().equals("")) {
                throw new IllegalArgumentException("Location cannot be empty.");
            }
            model.addAttribute("SuccessMessage", new InteractionPiAPI().setInteractionPiLocation(deviceLocation.get()));
            return "Feedback Pages/Success";
        } catch (IllegalArgumentException | RemoteException e) {
            model.addAttribute("ErrorMessage", e.getMessage());
            return "Feedback Pages/Error";
        }
    }

}
