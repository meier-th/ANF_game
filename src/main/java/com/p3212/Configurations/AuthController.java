package com.p3212.Configurations;

import com.p3212.EntityClasses.Role;
import com.p3212.EntityClasses.Stats;
import com.p3212.EntityClasses.User;
import com.p3212.EntityClasses.Character;
import com.p3212.EntityClasses.Message;
import com.p3212.Repositories.RoleRepository;
import com.p3212.Services.CharacterService;
import com.p3212.Services.StatsService;
import com.p3212.Services.UserService;

import java.util.Arrays;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

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
    
    @Autowired
    private WebSocketsController wsController;

    @Autowired
    private WebSocketsController notifServ;

    @Autowired
    private AuthenticationManager authManager;

    private static Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/checkCookies")
    public ResponseEntity checkCookies(@RequestParam String username) {
        try {
            if (SecurityContextHolder.getContext().getAuthentication().getName().equals(username))
                return ResponseEntity.status(HttpStatus.OK).body("{\"authorized\": true}");
            else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"authorized\": false}");
        } catch (Exception e) {
            logger.error("An exception occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"authorized\": false}");
        }
    }

    @PostMapping(value = "/registration")
    public ResponseEntity createNewUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        if (user.getLogin().equalsIgnoreCase("SYSTEM"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"text\":\"'SYSTEM' in any case is a reserved word. Users can not use it as their usernames.\"}");
        User userExists = userService.getUser(user.getLogin());
        if (userExists != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"text\":\"This username is already occupied\"}");
        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"text\":\"User object validation failed.\"}");
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
        /*Message warning = new Message();
        warning.setAuthor("SYSTEM");
        warning.setText("A user " + user.getLogin() + " has registered!");
        notifServ.notify(warning);*/
        String warning = "SYSTEM: A user " + user.getLogin() + " has registered!";
        notifServ.notify(warning);
//        UsernamePasswordAuthenticationToken authReq
//                = new UsernamePasswordAuthenticationToken(user, user.getPassword());
//        Authentication auth = authManager.authenticate(authReq);
//        SecurityContextHolder.getContext().setAuthentication(auth);
        // TODO set auth
        wsController.sendOnline("new:"+user.getLogin());
        return ResponseEntity.status(HttpStatus.CREATED).body("{\"text\":\"User successfully registered!\"}");
    }

    @PostMapping("/confirm")
    public ResponseEntity confirm(@RequestParam(name = "login") String login, @RequestParam(name = "password") String password) {
        if (login.equalsIgnoreCase("SYSTEM"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"text\":\"'SYSTEM' in any case is a reserved word. Users can not use it as their usernames.\"}");
        if (!userService.exists(login)) {
            if (password.length() >= 6 || password.isEmpty()) {
                User user = new User(login, password.isEmpty() ? null : password);
                String tmpName = SecurityContextHolder.getContext().getAuthentication().getName();
                if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("NEWGoogle"))) {
                    String email = tmpName.substring(3);
                    user.setEmail(email);
                } else {
                    int vkId = Integer.parseInt(tmpName.substring(3));
                    user.setVkId(vkId);
                }

                Stats stats = new Stats(50, 0, 0, 0, 0, 0, 1, 3);
                user.setStats(stats);
                statsService.addStats(stats);
                userService.saveUser(user);
                UsernamePasswordAuthenticationToken authReq
                        = new UsernamePasswordAuthenticationToken(user, password);
                Authentication auth = authManager.authenticate(authReq);
                SecurityContextHolder.getContext().setAuthentication(auth);
                return ResponseEntity.status(HttpStatus.CREATED).body("{\"text\":\"User successfully registered!\"}");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"text\":\"Password is too short.\"}");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"text\":\"This username is already occupied\"}");
    }

    @RequestMapping("/logout-success")
    public String logout() {
        // a method, that does nothing
        return "{\"text\":\"You are a stranger now.\"}";
    }

}
