package com.p3212.configuration;

import com.p3212.EntityClasses.Appearance;
import com.p3212.Services.AppearanceService;
import com.p3212.Services.CharacterService;
import com.p3212.Services.StatsService;
import java.util.List;
import com.p3212.EntityClasses.Character;
import com.p3212.EntityClasses.User;
import com.p3212.EntityClasses.Role;
import com.p3212.EntityClasses.Stats;
import com.p3212.Repositories.RoleRepository;
import com.p3212.Services.UserService;
import java.util.Arrays;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CharacterController {
    
    @Autowired
    AppearanceService appearanceServ;
    
    @Autowired
    CharacterService charServ;
    
    @Autowired
    RoleRepository roleRep;
    
    @Autowired
    StatsService statServ;
    
    @Autowired
    UserService userServ;
    
    @Autowired
    StatsService statsServ;
    
    @PostMapping("/characters/{id}/appearance")
    @ResponseBody public String addAppearance(@RequestBody String gender, @RequestBody String hair, @RequestBody String skin, @RequestBody String clothes, @PathVariable int id) {
        try {
            Appearance appear = new Appearance();
            appear.setCharact(charServ.getCharacter(id));
            appear.setClothesColour(Appearance.ClothesColour.valueOf(clothes));
            appear.setGender(Appearance.Gender.valueOf(gender));
            appear.setHairColour(Appearance.HairColour.valueOf(hair));
            appear.setSkinColour(Appearance.SkinColour.valueOf(skin));
            appearanceServ.addAppearance(appear);
            Character ch = charServ.getCharacter(id);
            ch.setAppearance(appear);
            charServ.addCharacter(ch);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }
    
    @GetMapping("/characters/{id}/appearance")
    @ResponseBody public Appearance getAppearance(@PathVariable int id) {
        return appearanceServ.getUserAppearance(id);
    }
    
    @DeleteMapping("/characters/{id}/appearance")
    @ResponseBody public String deleteAppearance(@PathVariable int id) {
        try {
            appearanceServ.removeUserAppearance(id);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }
    
    @GetMapping("/admin/characters")
    @ResponseBody public List<Character> getAllCharacters() {
        return charServ.getAllCharacters();
    }
    
    @PostMapping("/characters/{id}")
    @ResponseBody public String addCharacter(@PathVariable int id, @RequestBody String login) {
        try{
            Character ch = new Character(0.05f, 100, 10, 30);
            charServ.addCharacter(ch);
            User us = userServ.getUser(login);
            us.setCharacter(ch);
            userServ.saveUser(us);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }
    
    @GetMapping("/characters/{id}")
    @ResponseBody public Character getCharacter(@PathVariable int id) {
        return charServ.getCharacter(id);
    }
    
    @DeleteMapping("/characters/{id}")
    @ResponseBody public String deleteCharacter(@PathVariable int id) {
        try{
            charServ.removeCharacter(id);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }
    
    @PostMapping("/admin/users/{login}/grantAdmin")
    @ResponseBody public String grantAdmin(@PathVariable String login) {
        try {
            User user = userServ.getUser(login);
            Role usr = new Role();
            usr.setRole("USER");
            Role adm = new Role();
            adm.setRole("ADMIN");
            user.setRoles(new HashSet<>(Arrays.asList(usr, adm)));
            userServ.saveUser(user);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }
    
    @GetMapping("/admin/users")
    @ResponseBody public List<User> getAllUsers() {
        return userServ.getAllUsers();
    }
    
    @GetMapping("/users/{login}")
    @ResponseBody public User getUser(@PathVariable String login) {
        return userServ.getUser(login);
    }
    
    @DeleteMapping("/users/{login}")
    @ResponseBody public String deleteUser(@PathVariable String login) {
        try {
            userServ.removeUser(login);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }
    
    @PostMapping("/users/{login}/stats")
    @ResponseBody public String addOrUpdateStats(@PathVariable String login, @RequestBody int fights, @RequestBody int rating, @RequestBody int deaths, @RequestBody int wins,
                                         @RequestBody int losses, @RequestBody int experience, @RequestBody int level, @RequestBody int upgradePoints) {
        try {
            Stats sts = new Stats(rating, fights, wins, losses, deaths, experience, level, upgradePoints);
            statsServ.addStats(sts);
            User user = userServ.getUser(login);
            if (user.getStats() == null) {
                user.setStats(sts);
                userServ.saveUser(user);
            }
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }
    
    @DeleteMapping("/users/{login}/stats")
    @ResponseBody public String deleteStats(@PathVariable String login) {
        try {
            int id = userServ.getUser(login).getStats().getId();
            statsServ.removeStats(id);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }
    
    @GetMapping("/users/{login}/stats")
    @ResponseBody public Stats getStats(@PathVariable String login) {
        return userServ.getUser(login).getStats();
    }
    
}
