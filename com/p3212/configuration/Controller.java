package com.p3212.configuration;

import com.p3212.EntityClasses.User;
import com.p3212.Services.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class Controller {
    
    @Autowired
    private UserService userService;
    

    @RequestMapping(value="/admin/home", method = RequestMethod.GET)
    public ModelAndView adminHome(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUser(auth.getName());
        modelAndView.addObject("userName", "Welcome " + user.getLogin());
        modelAndView.addObject("adminMessage","Content Available Only for Users with Admin Role");
        modelAndView.setViewName("admin/home");
        return modelAndView;
    }
    
    @RequestMapping(value="/home", method = RequestMethod.GET)
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUser(auth.getName());
        modelAndView.addObject("greeting", "Welcome to your home page, " + user.getLogin());
        modelAndView.setViewName("home");
        return modelAndView;
    }
    
    @RequestMapping("/")
    public String reet() {
        return "start page";
    }
    
    @RequestMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
