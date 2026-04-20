package com.anf.infrastructure.web.rest;

import com.anf.domain.shared.ApiField;
import com.anf.domain.shared.ApiMessage;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Role;
import com.anf.model.database.Stats;
import com.anf.model.database.User;
import com.anf.infrastructure.persistence.repository.RoleRepository;
import com.anf.domain.user.CharacterService;
import com.anf.domain.user.StatsService;
import com.anf.domain.user.UserService;
import com.anf.configuration.WebSocketsController;
import java.util.Arrays;
import java.util.HashSet;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequiredArgsConstructor
public class AuthController {

  private static final String RESERVED_USERNAME = "SYSTEM";
  private static final String ANONYMOUS_USERNAME = "anonymoususer";
  private static final int MIN_PASSWORD_LENGTH = 6;
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
      if (!SecurityContextHolder.getContext()
          .getAuthentication()
          .getName()
          .equalsIgnoreCase(ANONYMOUS_USERNAME))
        return ResponseEntity.status(HttpStatus.OK)
            .body(
                "{\""
                    + ApiField.AUTHORIZED.getValue()
                    + "\": true, \""
                    + ApiField.LOGIN.getValue()
                    + "\":\""
                    + SecurityContextHolder.getContext().getAuthentication().getName()
                    + "\"}");
      else
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body("{\"" + ApiField.AUTHORIZED.getValue() + "\": false}");
    } catch (Exception e) {
      logger.error("An exception occurred: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("{\"" + ApiField.AUTHORIZED.getValue() + "\": false}");
    }
  }

  @PostMapping(value = "/registration")
  public ResponseEntity createNewUser(@RequestBody @Valid User user, BindingResult bindingResult) {
    if (user.getLogin().equalsIgnoreCase(RESERVED_USERNAME))
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("{\"" + ApiField.TEXT.getValue() + "\":\"" + ApiMessage.RESERVED_SYSTEM_USERNAME.getValue() + "\"}");
    if (bindingResult.hasErrors()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("{\"" + ApiField.TEXT.getValue() + "\":\"" + ApiMessage.USER_VALIDATION_FAILED.getValue() + "\"}");
    }
    User userExists = userService.getUser(user.getLogin());
    if (userExists != null) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body("{\"" + ApiField.TEXT.getValue() + "\":\"" + ApiMessage.USERNAME_OCCUPIED.getValue() + "\"}");
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
    return ResponseEntity.status(HttpStatus.CREATED)
        .body("{\"" + ApiField.TEXT.getValue() + "\":\"" + ApiMessage.USER_REGISTERED.getValue() + "\"}");
  }

  @PostMapping("/confirm")
  public ResponseEntity confirm(
      @RequestParam(name = "login") String login,
      @RequestParam(name = "password") String password) {
    if (login.equalsIgnoreCase(RESERVED_USERNAME))
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("{\"" + ApiField.TEXT.getValue() + "\":\"" + ApiMessage.RESERVED_SYSTEM_USERNAME.getValue() + "\"}");
    if (!userService.exists(login)) {
      if (password.length() >= MIN_PASSWORD_LENGTH || password.isEmpty()) {
        User user = new User(login, password.isEmpty() ? null : password);
        String tmpName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (SecurityContextHolder.getContext()
            .getAuthentication()
            .getAuthorities()
            .contains(new SimpleGrantedAuthority("NEWGoogle"))) {
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
        UsernamePasswordAuthenticationToken authReq =
            new UsernamePasswordAuthenticationToken(user, password);
        Authentication auth = authManager.authenticate(authReq);
        SecurityContextHolder.getContext().setAuthentication(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body("{\"" + ApiField.TEXT.getValue() + "\":\"" + ApiMessage.USER_REGISTERED.getValue() + "\"}");
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("{\"" + ApiField.TEXT.getValue() + "\":\"" + ApiMessage.PASSWORD_TOO_SHORT.getValue() + "\"}");
    }
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body("{\"" + ApiField.TEXT.getValue() + "\":\"" + ApiMessage.USERNAME_OCCUPIED.getValue() + "\"}");
  }

  @RequestMapping("/logout-success")
  public String logout() {
    return "{\"" + ApiField.TEXT.getValue() + "\":\"" + ApiMessage.USER_NOW_STRANGER.getValue() + "\"}";
  }
}
