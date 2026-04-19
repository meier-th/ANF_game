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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PvpAttackServiceTest {
  private SpellService spellService;
  private SpellKnowledgeService spellKnowledgeService;
  private StatsService statsService;
  private PVPFightsService pvpFightsService;
  private FightRuntimeStore fightStateStore;
  private FightSnapshotService fightSnapshotService;
  private FightStateNotifier fightStateNotifier;
  private PvpAttackService pvpAttackService;

  @BeforeEach
  void setUp() {
    spellService = mock(SpellService.class);
    spellKnowledgeService = mock(SpellKnowledgeService.class);
    statsService = mock(StatsService.class);
    pvpFightsService = mock(PVPFightsService.class);
    fightStateStore = mock(FightRuntimeStore.class);
    fightSnapshotService = mock(FightSnapshotService.class);
    fightStateNotifier = mock(FightStateNotifier.class);
    pvpAttackService =
        new PvpAttackService(
            spellService,
            spellKnowledgeService,
            statsService,
            pvpFightsService,
            fightStateStore,
            fightSnapshotService,
            fightStateNotifier);
  }

  @Test
  void attackPvp_returnsCode2_whenFightMissing() {
    when(fightStateStore.getFight("fight-1")).thenReturn(Optional.empty());

    Attack attack = pvpAttackService.attackPvp("alice", "bob", "fight-1", "Physical attack");

    assertThat(attack.getCode()).isEqualTo(2);
  }

  @Test
  void attackPvp_performsPhysicalAttack_andSavesFightWhenNotFinished() {
    var alice = user("alice1", 120);
    var bob = user("bobby2", 110);
    var fight = new FightPVP();
    fight.setFighters(alice.getCharacter(), bob.getCharacter());
    fight.switchAttacker(); // alice1
    when(fightStateStore.getFight("fight-1")).thenReturn(Optional.of(fight));

    Attack attack = pvpAttackService.attackPvp("alice1", "bobby2", "fight-1", "Physical attack");

    assertThat(attack.getCode()).isEqualTo(0);
    assertThat(attack.getDamage()).isGreaterThan(0);
    verify(fightStateStore).saveFight("fight-1", fight);
  }

  private User user(String login, int hp) {
    var user = new User();
    user.setLogin(login);
    user.setStats(new Stats(120, 0, 0, 0, 0, 0, 1, 3));
    var character = new GameCharacter(0.1f, hp, 10, 30);
    character.setUser(user);
    user.setCharacter(character);
    character.prepareForFight();
    return user;
  }
}
