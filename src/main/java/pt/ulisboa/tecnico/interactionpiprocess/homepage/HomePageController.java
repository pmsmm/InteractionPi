package pt.ulisboa.tecnico.interactionpiprocess.homepage;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import pt.ulisboa.tecnico.interactionpiprocess.api.MasterPi;

@Controller
@RequestMapping("/")
public class HomePageController {

    @RequestMapping(method = RequestMethod.GET)
    public RedirectView rootController() {
        return new RedirectView("/home");
    }

    @GetMapping("home")
    public String homePageController() {
        return "Home/Home";
    }

    @GetMapping("masterpi")
    public ModelAndView masterPiController(ModelMap model) {
        if (MasterPi.getInstance().getMasterPiAddress() == null) {
            model.addAttribute("ErrorMessage", "Interaction Pi is not connected to Central Pi");
            return new ModelAndView("Feedback Pages/Error");
        }
        return new ModelAndView("redirect:http://"+ MasterPi.getInstance().getMasterPiAddress() +":8081/home");
    }

}
