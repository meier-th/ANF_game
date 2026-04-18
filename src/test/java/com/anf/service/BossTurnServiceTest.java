package com.anf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.Boss;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Stats;
import com.anf.model.database.User;
import com.anf.service.state.LegacyFightRuntimeStore;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BossTurnServiceTest {
  private LegacyFightRuntimeStore fightStateStore;
  private FightSnapshotService fightSnapshotService;
  private InMemoryFightTurnScheduler fightTurnScheduler;
  private FightStateNotifier fightStateNotifier;
  private FightVsAIService fightVsAIService;
  private UserAIFightService userAiFightService;
  private StatsService statsService;
  private BossTurnService bossTurnService;

  @BeforeEach
  void setUp() {
    fightStateStore = mock(LegacyFightRuntimeStore.class);
    fightSnapshotService = mock(FightSnapshotService.class);
    fightTurnScheduler = mock(InMemoryFightTurnScheduler.class);
    fightStateNotifier = mock(FightStateNotifier.class);
    fightVsAIService = mock(FightVsAIService.class);
    userAiFightService = mock(UserAIFightService.class);
    statsService = mock(StatsService.class);
    bossTurnService =
        new BossTurnService(
            fightStateStore,
            fightSnapshotService,
            fightTurnScheduler,
            fightStateNotifier,
            fightVsAIService,
            userAiFightService,
            statsService);
  }

  @Test
  void handleBossAttack_savesFightAndSchedulesNextTurn_whenFightContinues() {
    var fight = new FightVsAI();
    var user = user("alice");
    fight.addFighter(user.getCharacter());
    fight.setSetFighters(new ArrayList<>(java.util.List.of(participation(fight, user))));
    var boss = mock(Boss.class);
    when(boss.getNumberOfTails()).thenReturn(1);
    fight.setBoss(boss);
    fight.switchAttacker();

    var nextTurnCalled = new AtomicBoolean(false);
    doAnswer(
            (invocation) -> {
              var task = invocation.getArgument(1, Runnable.class);
              task.run();
              return null;
            })
        .when(fightTurnScheduler)
        .schedule(eq("fight-1"), any(Runnable.class), any(Long.class), eq(TimeUnit.MILLISECONDS));

    bossTurnService.handleBossAttack(fight, "fight-1", () -> nextTurnCalled.set(true));

    assertThat(nextTurnCalled.get()).isTrue();
    verify(fightStateStore).saveFight("fight-1", fight);
  }

  private User user(String login) {
    var user = new User();
    user.setLogin(login);
    user.setStats(new Stats(100, 0, 0, 0, 0, 0, 1, 3));
    var character = new GameCharacter(0.9f, 200, 10, 30);
    character.setUser(user);
    user.setCharacter(character);
    character.prepareForFight();
    return user;
  }

  private AiFightParticipation participation(FightVsAI fight, User user) {
    var p = new AiFightParticipation();
    p.setFight(fight);
    p.setFighter(user.getCharacter());
    return p;
  }
}
