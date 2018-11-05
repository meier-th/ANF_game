package com.p3212.tst;

import EntityClasses.User;
import EntityClasses.Appearance;
import EntityClasses.Character;
import Services.AppearanceService;
import Services.CharacterService;
import Services.UserService;

import java.util.ArrayList;
import java.util.Iterator;
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
    @Autowired
    private AppearanceService appService;
    
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
    	Iterable<User> iterable = userService.getAllUsers();
    	List<User> toRet = new ArrayList<User>();
    	Iterator<User> iter = iterable.iterator();
    	while (iter.hasNext()) {
    		toRet.add(iter.next());
    	}
        return toRet;
    }
    
    @RequestMapping("/appearances") 
    public List<Appearance> getAllAppearances() {
    	Iterator<Appearance> iter = appService.getAllAppearances().iterator();
    	List<Appearance> toRet = new ArrayList<Appearance>();
    	while (iter.hasNext()) {
    		toRet.add(iter.next());
    	}
    	return toRet;
    }
    
    @RequestMapping("/persons")
    public List<Character> getAllCharacters() {
        return charService.getAllCharacters();
    }
}
