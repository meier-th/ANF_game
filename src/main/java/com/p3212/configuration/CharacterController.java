package com.p3212.configuration;

import com.p3212.EntityClasses.*;
import com.p3212.EntityClasses.Character;
import com.p3212.Services.*;

import java.util.List;

import com.p3212.Repositories.RoleRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

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

    @Autowired
    NinjaAnimalService ninjaAnimalServ;

    @PostMapping("/characters/{id}/appearance")
    @ResponseBody
    public String addAppearance(@RequestBody Appearance appear, @PathVariable int id) {
        try {
            appearanceServ.addAppearance(appear);
            Character ch = charServ.getCharacter(id);
            ch.setAppearance(appear);
            charServ.addCharacter(ch);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }

    @GetMapping("/characters/{id}/animals")
    public List<NinjaAnimal> getAvailableAnimals(@PathVariable int id) {
        Character character = charServ.getCharacter(id);
        if (character == null) return null;
        final int lvl = character.getUser().getStats().getLevel();
        NinjaAnimalRace race = character.getAnimalRace();
        return ninjaAnimalServ.list()
                .stream()
                .filter(animal -> animal.getRequiredLevel() <= lvl)
                .collect(Collectors.toList());
    }

    @GetMapping("/characters/{id}/appearance") // WE HAVE TO MAKE SURE ALL CHARACTERS HAVE THE SAME ID AS THEIR APPEARANCES, OTHERWISE WE'LL GET WRONG RESULTS HERE
    @ResponseBody
    public Appearance getAppearance(@PathVariable int id) {
        return appearanceServ.getUserAppearance(id);
    }

    @DeleteMapping("/characters/{id}/appearance")
    @ResponseBody
    public String deleteAppearance(@PathVariable int id) {
        try {
            appearanceServ.removeUserAppearance(id);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }

    @GetMapping("/admin/characters")
    @ResponseBody
    public List<Character> getAllCharacters() {
        return charServ.getAllCharacters();
    }

    @PostMapping("/characters/{id}") // after adding : /admin/users fails with Could not write JSON: (was java.lang.NullPointerException); nested exception is com.fasterxml.jackson.databind.JsonMappingException: (was java.lang.NullPointerException) (through reference chain: java.util.ArrayList[5]->com.p3212.EntityClasses.User[\"character\"]->com.p3212.EntityClass
    @ResponseBody
    public String addCharacter(@PathVariable int id, @RequestBody User us) {
        try {
            Character ch = new Character(0.05f, 100, 10, 30);
            ch.setId(id);
            charServ.addCharacter(ch);
            us.setCharacter(ch);
            userServ.saveUser(us);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }

    @GetMapping("/characters/{id}")
    @ResponseBody
    public Character getCharacter(@PathVariable int id) {
        return charServ.getCharacter(id);
    }

    @DeleteMapping("/characters/{id}") 
    @ResponseBody
    public String deleteCharacter(@PathVariable int id) {
        try {
            charServ.removeCharacter(id);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }

    @PostMapping("/admin/users/{login}/grantAdmin") //RETURNS OK, BUT DOESN'T WORK
    @ResponseBody
    public String grantAdmin(@PathVariable String login) {
        try {
            User user = userServ.getUser(login);
            user.addRole(new Role("ADMIN"));
            userServ.saveUser(user);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }

    @GetMapping("/admin/users")
    @ResponseBody
    public List<User> getAllUsers() {
        return userServ.getAllUsers();
    }

    @GetMapping("/users/{login}")
    @ResponseBody
    public User getUser(@PathVariable String login) {
        return userServ.getUser(login);
    }

    @DeleteMapping("/users/{login}") 
    @ResponseBody
    public String deleteUser(@PathVariable String login) {
        try {
            userServ.removeUser(login);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }

    @PostMapping("/users/{login}/stats")
    @ResponseBody
    public String addOrUpdateStats(@PathVariable String login, @RequestBody Stats sts) {
        try {
            statsServ.addStats(sts);
            User user = userServ.getUser(login);
            user.setStats(sts);
            userServ.saveUser(user);
            return "OK"; 
        } catch (Throwable error) {
            return error.getMessage();
        }
    }

    @DeleteMapping("/users/{login}/stats") 
    @ResponseBody
    public String deleteStats(@PathVariable String login) {
        try {
            int id = userServ.getUser(login).getStats().getId();
            statsServ.removeStats(id);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }

    @GetMapping("/users/{login}/stats")
    @ResponseBody
    public Stats getStats(@PathVariable String login) {
        return userServ.getUser(login).getStats();
    }

}
