package com.p3212.Configurations;

import com.p3212.Services.CharacterService;
import com.p3212.Services.StatsService;
import com.p3212.Services.UserService;
import com.p3212.Repositories.RoleRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Minimal smoke tests for {@link AuthController} to lock in
 * current behaviour of /checkCookies.
 *
 * These are deliberately narrow and will be replaced or
 * extended during the modernization work.
 */
public class AuthControllerTest {

    private AuthController controller;

    @Before
    public void setUp() {
        controller = new AuthController();
        // For the methods we test here, autowired collaborators are not used,
        // so we only need to ensure the controller instance can be created.
        // (Mockito mocks are created to make future extensions easier.)
        UserService userService = mock(UserService.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        CharacterService characterService = mock(CharacterService.class);
        StatsService statsService = mock(StatsService.class);
        WebSocketsController wsController = mock(WebSocketsController.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);

        // Use reflection to inject mocks without changing production code.
        // This keeps the test simple and non-invasive.
        TestReflection.setField(controller, "userService", userService);
        TestReflection.setField(controller, "roleRepository", roleRepository);
        TestReflection.setField(controller, "charServ", characterService);
        TestReflection.setField(controller, "statsService", statsService);
        TestReflection.setField(controller, "wsController", wsController);
        TestReflection.setField(controller, "notifServ", wsController);
        TestReflection.setField(controller, "authManager", authenticationManager);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void checkCookiesReturnsAuthorizedTrueWhenUserAuthenticated() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testUser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("USER"))
        );
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ResponseEntity response = controller.checkCookies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(String.valueOf(response.getBody()).contains("\"authorized\": true"));
        assertTrue(String.valueOf(response.getBody()).contains("\"login\":\"testUser\""));
    }

    @Test
    public void checkCookiesReturnsUnauthorizedWhenAnonymous() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "anonymoususer",
                "N/A",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        ResponseEntity response = controller.checkCookies();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(String.valueOf(response.getBody()).contains("\"authorized\": false"));
    }
}

