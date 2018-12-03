package com.p3212.Configurations;

import com.p3212.EntityClasses.Role;
import com.p3212.EntityClasses.Stats;
import com.p3212.EntityClasses.User;
import com.p3212.EntityClasses.Character;
import com.p3212.EntityClasses.Message;
import com.p3212.Repositories.RoleRepository;
import com.p3212.Services.CharacterService;
import com.p3212.Services.NotificationService;
import com.p3212.Services.StatsService;
import com.p3212.Services.UserService;

import java.util.Arrays;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private NotificationService notifServ;

    @Autowired
    private AuthenticationManager authManager;

    @PostMapping(value = "/registration")
    public ResponseEntity createNewUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        if (user.getLogin().equalsIgnoreCase("SYSTEM"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("'SYSTEM' in any case is a reserved word. Users can not use it as their usernames.");
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
        Message warning = new Message();
        warning.setFrom("SYSTEM");
        warning.setText("A user " + user.getLogin() + " has registered!");
        notifServ.notify(warning);
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully registered!");
    }

    @PostMapping("/confirm")
    public ResponseEntity confirm(@RequestParam(name = "login") String login, @RequestParam(name = "password") String password) {
        if (login.equalsIgnoreCase("SYSTEM"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("'SYSTEM' in any case is a reserved word. Users can not use it as their usernames.");
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
                return ResponseEntity.status(HttpStatus.CREATED).body("User successfully registered!");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password is too short.");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("This username is already occupied");
    }

}
