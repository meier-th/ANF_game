package com.anf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.configuration.WebSocketsController;
import com.anf.model.database.FightPVP;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Stats;
import com.anf.model.database.User;
import com.anf.infrastructure.state.FightRuntimeStore;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class FightTurnEngineServiceTest {
  private WebSocketsController webSocketsController;
  private FightRuntimeStore fightStateStore;
  private FightSnapshotService fightSnapshotService;
  private BossTurnService bossTurnService;
  private AnimalTurnService animalTurnService;
  private FightTurnEngineService fightTurnEngineService;

  @BeforeEach
  void setUp() {
    webSocketsController = mock(WebSocketsController.class);
    fightStateStore = mock(FightRuntimeStore.class);
    fightSnapshotService = mock(FightSnapshotService.class);
    bossTurnService = mock(BossTurnService.class);
    animalTurnService = mock(AnimalTurnService.class);
    fightTurnEngineService =
        new FightTurnEngineService(
            webSocketsController,
            fightStateStore,
            fightSnapshotService,
            bossTurnService,
            animalTurnService);
  }

  @Test
  void timeoutCurrentTurn_returnsConflict_whenTurnHasNotTimedOutYet() {
    var fight = pvpFight("alice111", "bobbbbb");
    fight.switchAttacker();
    when(fightStateStore.getFight("fight-1")).thenReturn(Optional.of(fight));
    when(fightSnapshotService.currentTurnStartedAt("fight-1")).thenReturn(System.currentTimeMillis());

    var response = fightTurnEngineService.timeoutCurrentTurn("fight-1", "bobbbbb", "alice111");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    verify(fightStateStore, never()).saveFight(any(), any());
  }

  @Test
  void timeoutCurrentTurn_isIdempotent_whenTurnAlreadyAdvanced() {
    var fight = pvpFight("alice111", "bobbbbb");
    fight.switchAttacker();
    when(fightStateStore.getFight("fight-1")).thenReturn(Optional.of(fight));

    var response = fightTurnEngineService.timeoutCurrentTurn("fight-1", "bobbbbb", "otherturn");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(fightSnapshotService, never()).timeoutCurrentTurnIfExpired(any(), any(), any(), any(Long.class), any(Long.class));
  }

  @Test
  void timeoutCurrentTurn_advancesTurn_whenExpired() {
    var fight = pvpFight("alice111", "bobbbbb");
    fight.switchAttacker();
    when(fightStateStore.getFight("fight-1")).thenReturn(Optional.of(fight));
    when(fightSnapshotService.timeoutCurrentTurnIfExpired(any(), any(), any(), any(Long.class), any(Long.class)))
        .thenReturn(FightSnapshotService.TimeoutReportResult.TIMED_OUT);

    var response = fightTurnEngineService.timeoutCurrentTurn("fight-1", "bobbbbb", "alice111");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(fight.getCurrentAttacker(0)).isEqualTo("bobbbbb");
    verify(fightStateStore).saveFight("fight-1", fight);
    verify(fightSnapshotService).syncFightSnapshot("fight-1", fight);
  }

  private FightPVP pvpFight(String user1, String user2) {
    var fight = new FightPVP();
    fight.setFighters(user(user1).getCharacter(), user(user2).getCharacter());
    return fight;
  }

  private User user(String login) {
    var user = new User();
    user.setLogin(login);
    user.setStats(new Stats(120, 0, 0, 0, 0, 0, 1, 3));
    var character = new GameCharacter(0.1f, 100, 10, 30);
    character.setUser(user);
    user.setCharacter(character);
    return user;
  }
}
