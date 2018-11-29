package com.p3212.configuration;

import com.p3212.EntityClasses.*;
import com.p3212.EntityClasses.Character;
import com.p3212.Services.*;
import java.util.List;
import com.p3212.Repositories.RoleRepository;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    UserService userServ;

    @Autowired
    StatsService statsServ;

    @Autowired
    NinjaAnimalService ninjaAnimalServ;

    @GetMapping("/profile")
    public ResponseEntity<String> myAccount() {
        try {
            String response = userServ.getUser(SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName()).toString();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.getMessage());
        }
    }

    @PostMapping("/profile/character/appearance")
    @ResponseBody
    public ResponseEntity<String> addAppearance(@RequestBody Appearance appear) {
        try {
            appearanceServ.addAppearance(appear);
            User user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            Character ch = user.getCharacter();
            ch.setAppearance(appear);
            charServ.addCharacter(ch);
            return ResponseEntity.status(HttpStatus.OK).body("Appearance is created.");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

    @GetMapping("/profile/character/animals")
    public ResponseEntity<?> getAvailableAnimals() {
        try {
            User user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            Character character = user.getCharacter();
            if (character == null) return null;
            final int lvl = character.getUser().getStats().getLevel();
            NinjaAnimalRace race = character.getAnimalRace();
            List<NinjaAnimal> animals =  ninjaAnimalServ.list()
                .stream()
                .filter(animal -> animal.getRequiredLevel() <= lvl)
                .filter(animal -> animal.getRace().equals(race))
                .collect(Collectors.toList());
            return ResponseEntity.status(HttpStatus.OK).body(animals);
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

    @GetMapping("/admin/characters")
    @ResponseBody
    public ResponseEntity<List<Character>> getAllCharacters() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(charServ.getAllCharacters());
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/profile/character")
    @ResponseBody
    public ResponseEntity<String> updateCharacter(@RequestBody Character ch) {
        try { 
            User us = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            us.setCharacter(ch);
            userServ.saveUser(us);
            return ResponseEntity.status(HttpStatus.OK).body("Character is updated.");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.getMessage());
        }
    }

    @GetMapping("/profile/character")
    @ResponseBody
    public ResponseEntity<?> getCharacter() {
        try {
            User user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.status(HttpStatus.OK).body(user.getCharacter());
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }
    
    @GetMapping("/users/{login}/character")
    @ResponseBody
    public ResponseEntity<?> getCharacter(@PathVariable String login) {
        try {
            User user = userServ.getUser(login);
            return ResponseEntity.status(HttpStatus.OK).body(user.getCharacter());
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }


    @PostMapping("/admin/users/{login}/grantAdmin")
    @ResponseBody
    public ResponseEntity<String> grantAdmin(@PathVariable String login) {
        try {
            User user = userServ.getUser(login);
            Role admin = roleRep.findById("ADMIN").get();
            user.addRole(admin);
            userServ.saveUser(user);
            return ResponseEntity.status(HttpStatus.OK).body("ADMIN role is granted for User "+user.getLogin()+".");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

    @GetMapping("/admin/users")
    @ResponseBody
    public ResponseEntity<?> getAllUsers() {
        try {
            List users = userServ.getAllUsers();
            return ResponseEntity.status(HttpStatus.OK).body(users);
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
        }
    }

    @GetMapping("/users/{login}")
    @ResponseBody
    public ResponseEntity<?> getUser(@PathVariable String login) {
        try {
            User user = userServ.getUser(login);
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

    @DeleteMapping("/profile")
    @ResponseBody
    public ResponseEntity<String> deleteUser() {
        try {
            String login = SecurityContextHolder.getContext().getAuthentication().getName();
            userServ.removeUser(login);
            return ResponseEntity.status(HttpStatus.OK).body("User is deleted.");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error.getMessage());
        }
    }

    @PostMapping("/profile/stats")
    @ResponseBody
    public ResponseEntity<String> addOrUpdateStats(@RequestBody Stats sts) {
        try {
            statsServ.addStats(sts);
            User user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            user.setStats(sts);
            userServ.saveUser(user);
            return ResponseEntity.status(HttpStatus.OK).body("Stats are altered."); 
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

    @GetMapping("/users/{login}/stats")
    @ResponseBody
    public ResponseEntity<?> getStats(@PathVariable String login) {
        try {
            Stats stats = userServ.getUser(login).getStats();
        return ResponseEntity.status(HttpStatus.OK).body(stats);
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

}
