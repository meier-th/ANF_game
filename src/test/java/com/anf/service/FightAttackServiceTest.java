package com.anf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.model.Attack;
import com.anf.model.database.FightPVP;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Stats;
import com.anf.model.database.User;
import com.anf.service.state.FightRuntimeStore;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class FightAttackServiceTest {
  private FightSnapshotService fightSnapshotService;
  private FightRuntimeStore fightStateStore;
  private FightAttackService fightAttackService;

  @BeforeEach
  void setUp() {
    fightSnapshotService = mock(FightSnapshotService.class);
    fightStateStore = mock(FightRuntimeStore.class);
    fightAttackService = new FightAttackService(fightSnapshotService, fightStateStore);
  }

  @Test
  void attack_returnsNotFound_whenProtobufStateMissing() {
    var context = new FightAttackService.AttackContext("alice", "bob", "fight-1", "Physical attack");
    when(fightSnapshotService.hasProtobufState("fight-1")).thenReturn(false);

    var response = fightAttackService.attack(context, (ctx) -> new Attack(), (ctx) -> new Attack(), () -> {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    verify(fightStateStore, never()).getFight(any());
  }

  @Test
  void attack_returnsForbidden_whenNotPlayersTurn() {
    var context = new FightAttackService.AttackContext("alice", "bob", "fight-1", "Physical attack");
    when(fightSnapshotService.hasProtobufState("fight-1")).thenReturn(true);
    var fight = pvpFight("bob", "alice");
    fight.switchAttacker(); // current attacker = bob
    when(fightStateStore.getFight("fight-1")).thenReturn(Optional.of(fight));
    when(fightSnapshotService.isCurrentAttacker("fight-1", "alice", "bob")).thenReturn(false);

    var response = fightAttackService.attack(context, (ctx) -> new Attack(), (ctx) -> new Attack(), () -> {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void attack_usesPvpExecutor_andSchedulesNextTurn() {
    var context = new FightAttackService.AttackContext("alice", "bob", "fight-1", "Physical attack");
    when(fightSnapshotService.hasProtobufState("fight-1")).thenReturn(true);
    var fight = pvpFight("alice", "bob");
    fight.switchAttacker(); // current attacker = alice
    when(fightStateStore.getFight("fight-1")).thenReturn(Optional.of(fight));
    when(fightSnapshotService.isCurrentAttacker("fight-1", "alice", "alice")).thenReturn(true);

    var pvpCalled = new AtomicBoolean(false);
    var scheduled = new AtomicBoolean(false);
    var attack = new Attack();
    attack.setCode(0);

    var response =
        fightAttackService.attack(
            context,
            (ctx) -> {
              pvpCalled.set(true);
              return attack;
            },
            (ctx) -> {
              throw new IllegalStateException("PvE callback should not run");
            },
            () -> scheduled.set(true));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(pvpCalled.get()).isTrue();
    assertThat(scheduled.get()).isTrue();
    verify(fightSnapshotService).syncFightSnapshot("fight-1", fight);
  }

  @Test
  void attack_returnsBadRequest_whenAttackCodeIsNotZero() {
    var context = new FightAttackService.AttackContext("alice", "bob", "fight-1", "Physical attack");
    when(fightSnapshotService.hasProtobufState("fight-1")).thenReturn(true);
    var fight = pvpFight("alice", "bob");
    fight.switchAttacker(); // current attacker = alice
    when(fightStateStore.getFight("fight-1")).thenReturn(Optional.of(fight));
    when(fightSnapshotService.isCurrentAttacker("fight-1", "alice", "alice")).thenReturn(true);

    var attack = new Attack();
    attack.setCode(8);
    var scheduled = new AtomicBoolean(false);

    var response = fightAttackService.attack(context, (ctx) -> attack, (ctx) -> attack, () -> scheduled.set(true));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(scheduled.get()).isFalse();
  }

  private FightPVP pvpFight(String fighter1Login, String fighter2Login) {
    var fight = new FightPVP();
    fight.setFighters(user(fighter1Login).getCharacter(), user(fighter2Login).getCharacter());
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
