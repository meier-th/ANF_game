package com.p3212.tst;

import EntityClasses.User;
import EntityClasses.Character;
import Services.CharacterService;
import Services.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    
    @Autowired
    private UserService userService;
    @Autowired
    private CharacterService charService;
    
    @RequestMapping("/testing")
    public String ret() {
        return "It works";
    }
    
    @RequestMapping("/")
    public String reet() {
        return "start page";
    }
    
    @RequestMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    @RequestMapping("/persons")
    public List<Character> getAllCharacters() {
        return charService.getAllCharacters();
    }
}
