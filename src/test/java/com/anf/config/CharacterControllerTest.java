package com.anf.config;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.anf.model.GameCharacter;
import com.anf.model.Stats;
import com.anf.model.User;
import com.anf.repository.RoleRepository;
import com.anf.service.AppearanceService;
import com.anf.service.CharacterService;
import com.anf.service.StatsService;
import com.anf.service.UserService;

/**
 * Unit tests for {@link CharacterController} covering character stat upgrades.
 */
public class CharacterControllerTest {

    private UserService userService;
    private CharacterService characterService;
    private StatsService statsService;
    private CharacterController controller;

    @Before
    public void setUp() {
        userService = mock(UserService.class);
        characterService = mock(CharacterService.class);
        statsService = mock(StatsService.class);

        controller = new CharacterController(
                mock(AppearanceService.class),
                characterService,
                mock(RoleRepository.class),
                userService,
                statsService,
                mock(WebSocketsController.class)
        );
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // --- POST /profile/character (upgrade stat) ---

    @Test
    public void updateCharacter_increasesPhysicalDamage() {
        User user = userWithCharacterAndPoints(1);
        user.getCharacter().setPhysicalDamage(10);
        setSecurityContext("alice");
        when(userService.getUser("alice")).thenReturn(user);

        ResponseEntity<String> response = controller.updateCharacter("damage");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(14, user.getCharacter().getPhysicalDamage());
        assertEquals(0, user.getStats().getUpgradePoints());
        verify(characterService).addCharacter(user.getCharacter());
        verify(statsService).addStats(user.getStats());
    }

    @Test
    public void updateCharacter_increasesMaxHp() {
        User user = userWithCharacterAndPoints(1);
        user.getCharacter().setMaxHP(100);
        setSecurityContext("alice");
        when(userService.getUser("alice")).thenReturn(user);

        ResponseEntity<String> response = controller.updateCharacter("hp");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(115, user.getCharacter().getMaxHp());
    }

    @Test
    public void updateCharacter_increasesResistance() {
        User user = userWithCharacterAndPoints(1);
        user.getCharacter().setResistance(0.0f);
        setSecurityContext("alice");
        when(userService.getUser("alice")).thenReturn(user);

        ResponseEntity<String> response = controller.updateCharacter("resistance");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // new resistance = 0 + (1-0)/4 = 0.25
        assertEquals(0.25f, user.getCharacter().getResistance(), 0.001f);
    }

    @Test
    public void updateCharacter_increasesMaxChakra() {
        User user = userWithCharacterAndPoints(1);
        user.getCharacter().setMaxChakraAmount(30);
        setSecurityContext("alice");
        when(userService.getUser("alice")).thenReturn(user);

        ResponseEntity<String> response = controller.updateCharacter("chakra");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(37, user.getCharacter().getMaxChakra());
    }

    @Test
    public void updateCharacter_returnsForbidden_whenNoUpgradePoints() {
        User user = userWithCharacterAndPoints(0);
        setSecurityContext("alice");
        when(userService.getUser("alice")).thenReturn(user);

        ResponseEntity<String> response = controller.updateCharacter("damage");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(characterService, never()).addCharacter(any());
        verify(statsService, never()).addStats(any());
    }

    @Test
    public void updateCharacter_returnsNotFound_forUnknownQuality() {
        User user = userWithCharacterAndPoints(3);
        setSecurityContext("alice");
        when(userService.getUser("alice")).thenReturn(user);

        ResponseEntity<String> response = controller.updateCharacter("speed");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(characterService, never()).addCharacter(any());
    }

    @Test
    public void updateCharacter_consumesExactlyOneUpgradePoint() {
        User user = userWithCharacterAndPoints(5);
        setSecurityContext("alice");
        when(userService.getUser("alice")).thenReturn(user);

        controller.updateCharacter("damage");

        assertEquals(4, user.getStats().getUpgradePoints());
    }

    // helpers

    private void setSecurityContext(String name) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                name, "N/A",
                Collections.singletonList(new SimpleGrantedAuthority("USER"))
        ));
        SecurityContextHolder.setContext(context);
    }

    private User userWithCharacterAndPoints(int upgradePoints) {
        Stats stats = new Stats(50, 0, 0, 0, 0, 0, 1, upgradePoints);
        User user = new User();
        user.setStats(stats);
        GameCharacter ch = new GameCharacter(0.1f, 100, 10, 30);
        user.setCharacter(ch);
        return user;
    }
}
