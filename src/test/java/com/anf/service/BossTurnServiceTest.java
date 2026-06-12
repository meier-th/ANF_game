package com.anf.service;

import com.anf.domain.auth.*;
import com.anf.domain.combat.*;
import com.anf.domain.fight.*;
import com.anf.domain.social.*;
import com.anf.domain.user.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.Boss;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Stats;
import com.anf.model.database.User;
import com.anf.infrastructure.state.FightRuntimeStore;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BossTurnServiceTest {
  private FightRuntimeStore fightStateStore;
  private FightSnapshotService fightSnapshotService;
  private FightStateNotifier fightStateNotifier;
  private FightDamageService fightDamageService;
  private FightStatsUpdateService fightStatsUpdateService;
  private BossTurnService bossTurnService;

  @BeforeEach
  void setUp() {
    fightStateStore = mock(FightRuntimeStore.class);
    fightSnapshotService = mock(FightSnapshotService.class);
    fightStateNotifier = mock(FightStateNotifier.class);
    fightDamageService = mock(FightDamageService.class);
    fightStatsUpdateService = mock(FightStatsUpdateService.class);
    bossTurnService =
        new BossTurnService(
            fightStateStore,
            fightSnapshotService,
            fightStateNotifier,
            fightDamageService,
            fightStatsUpdateService);

    when(fightDamageService.computeBossAttackDamage(any(Integer.class), any(Float.class))).thenReturn(20);
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
    bossTurnService.handleBossAttack(fight, "fight-1", () -> nextTurnCalled.set(true));

    assertThat(nextTurnCalled.get()).isTrue();
    verify(fightStateStore).saveFight("fight-1", fight);
  }

  @Test
  void handleBossAttack_usesFighterLogins_whenParticipationBackrefsMissing() {
    var fight = new FightVsAI();
    var user = user("alice");
    fight.addFighter(user.getCharacter());

    var detachedCharacter = new GameCharacter(0.9f, 200, 10, 30);
    var detachedParticipation = new AiFightParticipation();
    detachedParticipation.setFight(fight);
    detachedParticipation.setFighter(detachedCharacter);
    fight.setSetFighters(new ArrayList<>(java.util.List.of(detachedParticipation)));

    var boss = mock(Boss.class);
    when(boss.getNumberOfTails()).thenReturn(1);
    fight.setBoss(boss);
    fight.switchAttacker();

    var nextTurnCalled = new AtomicBoolean(false);
    bossTurnService.handleBossAttack(fight, "fight-2", () -> nextTurnCalled.set(true));

    assertThat(nextTurnCalled.get()).isTrue();
    verify(fightStateNotifier)
        .sendAfterAttack(
            eq("alice"), any(Integer.class), any(), any(), any(), any(Boolean.class), any(Boolean.class), any(), eq(0), eq(0));
  }

  @Test
  void handleBossAttack_notifiesAndUnmarksKilledLastFighter_whenFightEnds() {
    var fight = new FightVsAI();
    var user = user("alice");
    fight.addFighter(user.getCharacter());
    fight.setSetFighters(new ArrayList<>(java.util.List.of(participation(fight, user))));
    var boss = mock(Boss.class);
    when(boss.getNumberOfTails()).thenReturn(1);
    fight.setBoss(boss);
    fight.switchAttacker();
    when(fightDamageService.computeBossAttackDamage(any(Integer.class), any(Float.class))).thenReturn(9999);

    var nextTurnCalled = new AtomicBoolean(false);
    bossTurnService.handleBossAttack(fight, "fight-3", () -> nextTurnCalled.set(true));

    assertThat(nextTurnCalled.get()).isFalse();
    verify(fightStateNotifier)
        .sendAfterAttack(
            eq("alice"), any(Integer.class), eq("alice"), any(), any(), eq(true), eq(true), any(), eq(0), eq(0));
    verify(fightStateStore).unmarkUserInFight("alice");
    verify(fightStateStore, never()).saveFight("fight-3", fight);
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
