package com.anf.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.model.GameCharacter;
import com.anf.model.Role;
import com.anf.model.Stats;
import com.anf.model.User;
import com.anf.repository.RoleRepository;
import com.anf.service.CharacterService;
import com.anf.service.StatsService;
import com.anf.service.UserService;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;

/**
 * @ Unit tests for {@link AuthController} covering registration, session checks, and reserved-name
 * guard.
 */
public class AuthControllerTest {

  private UserService userService;
  private RoleRepository roleRepository;
  private CharacterService characterService;
  private StatsService statsService;
  private WebSocketsController wsController;
  private AuthenticationManager authManager;
  private AuthController controller;
  private BindingResult bindingResult;

  @BeforeEach
  public void setUp() {
    userService = mock(UserService.class);
    roleRepository = mock(RoleRepository.class);
    characterService = mock(CharacterService.class);
    statsService = mock(StatsService.class);
    wsController = mock(WebSocketsController.class);
    authManager = mock(AuthenticationManager.class);
    bindingResult = mock(BindingResult.class);

    controller =
        new AuthController(
            userService, roleRepository, characterService, statsService, wsController, authManager);

    Role userRole = new Role();
    when(roleRepository.findById("USER")).thenReturn(Optional.of(userRole));
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  // --- /checkCookies ---

  @Test
  public void checkCookies_returnsAuthorized_whenUserAuthenticated() {
    setSecurityContext("alice", "USER");

    ResponseEntity response = controller.checkCookies();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("\"authorized\": true"));
    assertTrue(response.getBody().toString().contains("\"login\":\"alice\""));
  }

  @Test
  public void checkCookies_returnsUnauthorized_whenAnonymous() {
    setSecurityContext("anonymoususer", "ROLE_ANONYMOUS");

    ResponseEntity response = controller.checkCookies();

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("\"authorized\": false"));
  }

  // --- /registration ---

  @Test
  public void registration_createsUser_onHappyPath() {
    when(bindingResult.hasErrors()).thenReturn(false);
    when(userService.getUser("alice")).thenReturn(null);

    User user = new User("alice", "password123");
    ResponseEntity response = controller.createNewUser(user, bindingResult);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("successfully registered"));

    verify(statsService).addStats(any(Stats.class));
    verify(characterService).addCharacter(any(GameCharacter.class));
    verify(userService).saveUser(user);
    verify(wsController).sendOnline("new:alice");
  }

  @Test
  public void registration_rejectsSYSTEMUsername_caseInsensitive() {
    User user = new User("SyStEm", "password123");
    when(bindingResult.hasErrors()).thenReturn(false);

    ResponseEntity response = controller.createNewUser(user, bindingResult);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("reserved word"));
    verify(userService, never()).saveUser(any());
  }

  @Test
  public void registration_rejectsValidationErrors() {
    User user = new User("alice", "x");
    when(bindingResult.hasErrors()).thenReturn(true);

    ResponseEntity response = controller.createNewUser(user, bindingResult);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("validation failed"));
    verify(userService, never()).saveUser(any());
  }

  @Test
  public void registration_rejectsDuplicateUsername() {
    User existing = new User("alice", "pass");
    when(userService.getUser("alice")).thenReturn(existing);
    when(bindingResult.hasErrors()).thenReturn(false);

    User user = new User("alice", "newpass");
    ResponseEntity response = controller.createNewUser(user, bindingResult);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("already occupied"));
    verify(userService, never()).saveUser(any());
  }

  @Test
  public void registration_assignsInitialStatsAndCharacter() {
    when(bindingResult.hasErrors()).thenReturn(false);
    when(userService.getUser("bob")).thenReturn(null);

    User user = new User("bob", "password123");
    controller.createNewUser(user, bindingResult);

    // Stats starts at rating 50, level 1, 3 upgrade points
    ArgumentCaptor<Stats> statsCaptor = ArgumentCaptor.forClass(Stats.class);
    verify(statsService).addStats(statsCaptor.capture());
    Stats created = statsCaptor.getValue();
    assertEquals(50, created.getRating());
    assertEquals(1, created.getLevel());
    assertEquals(3, created.getUpgradePoints());

    // Character starts with 5% resistance, 100 HP, 10 damage, 30 chakra
    ArgumentCaptor<GameCharacter> charCaptor = ArgumentCaptor.forClass(GameCharacter.class);
    verify(characterService).addCharacter(charCaptor.capture());
    GameCharacter ch = charCaptor.getValue();
    assertEquals(0.05f, ch.getResistance(), 0.001f);
    assertEquals(100, ch.getMaxHp());
    assertEquals(10, ch.getPhysicalDamage());
    assertEquals(30, ch.getMaxChakra());
  }

  // --- /logout-success ---

  @Test
  public void logout_returnsStrangerMessage() {
    String result = controller.logout();
    assertTrue(result.contains("stranger"));
  }

  // helper

  private void setSecurityContext(String name, String role) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(
        new UsernamePasswordAuthenticationToken(
            name, "N/A", Collections.singletonList(new SimpleGrantedAuthority(role))));
    SecurityContextHolder.setContext(context);
  }
}
