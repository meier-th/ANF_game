package com.anf.config;

import java.util.Arrays;
import java.util.HashSet;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anf.model.GameCharacter;
import com.anf.model.Role;
import com.anf.model.Stats;
import com.anf.model.User;
import com.anf.repository.RoleRepository;
import com.anf.service.CharacterService;
import com.anf.service.StatsService;
import com.anf.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final CharacterService charServ;
    private final StatsService statsService;
    private final WebSocketsController wsController;
    private final AuthenticationManager authManager;

    @GetMapping("/checkCookies")
    public ResponseEntity checkCookies() {
        try {
            if (!SecurityContextHolder.getContext().getAuthentication().getName().equalsIgnoreCase("anonymoususer"))
                return ResponseEntity.status(HttpStatus.OK).body("{\"authorized\": true, \"login\":\"" +
                        SecurityContextHolder.getContext().getAuthentication().getName() + "\"}");
            else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"authorized\": false}");
        } catch (Exception e) {
            logger.error("An exception occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"authorized\": false}");
        }
    }

    @PostMapping(value = "/registration")
    public ResponseEntity createNewUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        if (user.getLogin().equalsIgnoreCase("SYSTEM"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"text\":\"'SYSTEM' in any case is a reserved word. Users can not use it as their usernames.\"}");
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"text\":\"User object validation failed.\"}");
        }
        User userExists = userService.getUser(user.getLogin());
        if (userExists != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"text\":\"This username is already occupied\"}");
        }
        Stats stats = new Stats(50, 0, 0, 0, 0, 0, 1, 3);
        user.setStats(stats);
        statsService.addStats(stats);
        Role userRole = roleRepository.findById("USER").get();
        user.setRoles(new HashSet<>(Arrays.asList(userRole)));
        GameCharacter ch = new GameCharacter(0.05f, 100, 10, 30);
        charServ.addCharacter(ch);
        user.setCharacter(ch);
        userService.saveUser(user);
        String warning = "SYSTEM: A user " + user.getLogin() + " has registered!";
        wsController.notify(warning);
        wsController.sendOnline("new:" + user.getLogin());
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
        return "{\"text\":\"You are a stranger now.\"}";
    }
}
