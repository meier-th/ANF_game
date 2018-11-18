package com.p3212.configuration;

import com.p3212.EntityClasses.User;
import com.p3212.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;

@RestController()
public class AuthController {
    @Autowired
    private UserService userService;

    @RequestMapping(value = {"/login"}, method = RequestMethod.GET)
    public ModelAndView login() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @RequestMapping(value = {"/kek"})
    public String kek() {
        System.out.println("kek");
        return "redirect:/characters/3/animals";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public ModelAndView registration() {
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("registration");
        return modelAndView;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        User userExists = userService.getUser(user.getLogin());
        if (userExists != null) {
            bindingResult
                    .rejectValue("login", "error.user",
                            "There is already a user registered with the login provided");
        }
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("registration");
        } else {
            userService.saveUser(user);
            modelAndView.addObject("successMessage", "User has been registered successfully");
            modelAndView.addObject("user", new User());
            modelAndView.setViewName("registration");

        }
        return modelAndView;
    }

    @RequestMapping(value = "/registerVk", method = RequestMethod.GET)
    public RedirectView registerVk(RedirectAttributes attributes) {
        attributes.addAttribute("client_id", "6751264");
        attributes.addAttribute("redirect_uri", "http://localhost:8080/getVkCode");
        attributes.addAttribute("display", "popup");
        return new RedirectView("https://oauth.vk.com/authorize");
    }

    @RequestMapping("/getVkCode")
    public RedirectView getVkCode(@RequestParam(name = "code") String code, RedirectAttributes attributes) {
        attributes.addAttribute("client_id", "6751264");
        attributes.addAttribute("client_secret", "YqVhWS11S17pz670MHzG");
        attributes.addAttribute("redirect_uri", "http://localhost:8080/getVkCode");
        attributes.addAttribute("code", code);
        return new RedirectView("https://oauth.vk.com/access_token");
    }

}
