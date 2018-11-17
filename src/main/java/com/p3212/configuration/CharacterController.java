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

    @GetMapping("/characters/{id}/appearance")
    @ResponseBody
    public Appearance getAppearance(@PathVariable int id) {
        return appearanceServ.getUserAppearance(id);
    }

    @DeleteMapping("/characters/{id}/appearance") //  Key (id)=(1) is still referenced from table "persons"
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

    @PostMapping("/characters/{id}")
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

    @DeleteMapping("/characters/{id}") //Key (id)=(2) is still referenced from table "pvp_fights"
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
    @ResponseBody
    public List<User> getAllUsers() {
        return userServ.getAllUsers();
    }

    @GetMapping("/users/{login}")
    @ResponseBody
    public User getUser(@PathVariable String login) {
        return userServ.getUser(login);
    }

    @DeleteMapping("/users/{login}") // Key (role)=(USER) is still referenced from table "user_role".
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
            User user = userServ.getUser(login);
            if (user.getStats() == null) {
                statsServ.addStats(sts);
                user.setStats(sts);
                userServ.saveUser(user);
            }
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }

    @DeleteMapping("/users/{login}/stats") // Key (id)=(1) is still referenced from table "users".
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
