package com.p3212.configuration;

import com.p3212.EntityClasses.Role;
import com.p3212.EntityClasses.Stats;
import com.p3212.EntityClasses.User;
import com.p3212.EntityClasses.Character;
import com.p3212.Repositories.RoleRepository;
import com.p3212.Services.CharacterService;
import com.p3212.Services.StatsService;
import com.p3212.Services.UserService;
import java.util.Arrays;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController()
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private CharacterService charServ;
    
    @Autowired
    private StatsService statsService;
    
    @PostMapping(value = "/registration")
    public ResponseEntity createNewUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        User userExists = userService.getUser(user.getLogin());
        if (userExists != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This username is already occupied");
        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User object validation failed.");
        }
        Stats stats = new Stats(50, 0, 0, 0, 0, 0, 1, 3);
        user.setStats(stats);
        statsService.addStats(stats);
        Role userRole = roleRepository.findById("USER").get();
        user.setRoles(new HashSet<>(Arrays.asList(userRole)));
        Character ch = new Character(0.05f, 100, 10, 30);
        charServ.addCharacter(ch);
        user.setCharacter(ch);
        userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully registered!");
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
