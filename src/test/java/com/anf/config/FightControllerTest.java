package com.anf.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.anf.service.BossService;
import com.anf.service.FightLobbyService;
import com.anf.service.FightSnapshotService;
import com.anf.service.FightStartService;
import com.anf.service.FightVsAIService;
import com.anf.service.InMemoryFightTurnScheduler;
import com.anf.service.NinjaAnimalService;
import com.anf.service.PVPFightsService;
import com.anf.service.SpellKnowledgeService;
import com.anf.service.SpellService;
import com.anf.service.StatsService;
import com.anf.service.UserAIFightService;
import com.anf.service.UserService;
import com.anf.service.state.LegacyFightRuntimeStore;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class FightControllerTest {
  private FightLobbyService fightLobbyService;
  private FightController fightController;

  @BeforeEach
  void setUp() {
    fightLobbyService = mock(FightLobbyService.class);
    fightController =
        new FightController(
            mock(UserService.class),
            mock(SpellService.class),
            mock(BossService.class),
            mock(PVPFightsService.class),
            mock(FightVsAIService.class),
            mock(UserAIFightService.class),
            mock(NinjaAnimalService.class),
            mock(SpellKnowledgeService.class),
            mock(StatsService.class),
            mock(WebSocketsController.class),
            fightLobbyService,
            mock(FightSnapshotService.class),
            mock(FightStartService.class),
            mock(InMemoryFightTurnScheduler.class),
            mock(LegacyFightRuntimeStore.class));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createLobby_usesAuthenticatedUserAsLeader() {
    setSecurityContext("alice");
    ResponseEntity<?> expected =
        ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of("lobbyUuid", "lobby-1"));
    doReturn(expected).when(fightLobbyService).createLobby("PVP", "alice");

    var response = fightController.createLobby("PVP");

    assertThat(response).isEqualTo(expected);
    verify(fightLobbyService).createLobby("PVP", "alice");
  }

  @Test
  void joinLobby_usesAuthenticatedUser() {
    setSecurityContext("bob");
    ResponseEntity<?> expected = ResponseEntity.ok(java.util.Map.of("answer", "OK"));
    doReturn(expected).when(fightLobbyService).joinLobby("lobby-2", "bob");

    var response = fightController.joinLobby("lobby-2");

    assertThat(response).isEqualTo(expected);
    verify(fightLobbyService).joinLobby("lobby-2", "bob");
  }

  @Test
  void leaveLobby_usesAuthenticatedUser() {
    setSecurityContext("charlie");
    ResponseEntity<?> expected = ResponseEntity.ok(java.util.Map.of("answer", "LEFT"));
    doReturn(expected).when(fightLobbyService).leaveLobby("lobby-3", "charlie");

    var response = fightController.leaveLobby("lobby-3");

    assertThat(response).isEqualTo(expected);
    verify(fightLobbyService).leaveLobby("lobby-3", "charlie");
  }

  @Test
  void closeLobby_delegatesToService_andReturnsNoContent() {
    var response = fightController.closeLobbyV2("lobby-4");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(fightLobbyService).closeLobby("lobby-4");
  }

  private void setSecurityContext(String username) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(
        new UsernamePasswordAuthenticationToken(
            username, "N/A", Collections.singletonList(new SimpleGrantedAuthority("USER"))));
    SecurityContextHolder.setContext(context);
  }
}
