package com.p3212.tst;

import EntityClasses.User;
import Services.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    
    @RequestMapping("/testing")
    public String ret() {
        return "It works";
    }
    
    @Autowired
    private UserService userService;
    
    @RequestMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
