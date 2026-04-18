package com.anf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.model.Attack;
import com.anf.model.database.Boss;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Stats;
import com.anf.model.database.User;
import com.anf.service.state.LegacyFightRuntimeStore;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PveAttackServiceTest {
  private UserService userService;
  private SpellService spellService;
  private SpellKnowledgeService spellKnowledgeService;
  private FightVsAIService fightVsAIService;
  private UserAIFightService userAiFightService;
  private StatsService statsService;
  private LegacyFightRuntimeStore fightStateStore;
  private FightSnapshotService fightSnapshotService;
  private FightStateNotifier fightStateNotifier;
  private PveAttackService pveAttackService;

  @BeforeEach
  void setUp() {
    userService = mock(UserService.class);
    spellService = mock(SpellService.class);
    spellKnowledgeService = mock(SpellKnowledgeService.class);
    fightVsAIService = mock(FightVsAIService.class);
    userAiFightService = mock(UserAIFightService.class);
    statsService = mock(StatsService.class);
    fightStateStore = mock(LegacyFightRuntimeStore.class);
    fightSnapshotService = mock(FightSnapshotService.class);
    fightStateNotifier = mock(FightStateNotifier.class);
    pveAttackService =
        new PveAttackService(
            userService,
            spellService,
            spellKnowledgeService,
            fightVsAIService,
            userAiFightService,
            statsService,
            fightStateStore,
            fightSnapshotService,
            fightStateNotifier);
  }

  @Test
  void attackPve_returnsCode2_whenFightMissing() {
    when(fightStateStore.getFight("fight-1")).thenReturn(Optional.empty());

    Attack attack = pveAttackService.attackPve("alice1", "fight-1", "Physical attack");

    assertThat(attack.getCode()).isEqualTo(2);
  }

  @Test
  void attackPve_performsPhysicalAttack_andSavesFightWhenBossLives() {
    var attacker = user("alice1");
    when(userService.getUser("alice1")).thenReturn(attacker);
    var fight = new FightVsAI();
    fight.addFighter(attacker.getCharacter());
    var boss = mock(Boss.class);
    when(boss.getResistance()).thenReturn(0.1f);
    when(boss.getCurrentHP()).thenReturn(1000);
    when(boss.getNumberOfTails()).thenReturn(1);
    fight.setBoss(boss);
    fight.switchAttacker();
    when(fightStateStore.getFight("fight-1")).thenReturn(Optional.of(fight));

    Attack attack = pveAttackService.attackPve("alice1", "fight-1", "Physical attack");

    assertThat(attack.getCode()).isEqualTo(0);
    assertThat(attack.getDamage()).isGreaterThan(0);
    verify(fightStateStore).saveFight("fight-1", fight);
    verify(fightSnapshotService, never()).deleteFightArtifacts(any(), any());
  }

  private User user(String login) {
    var user = new User();
    user.setLogin(login);
    user.setStats(new Stats(120, 0, 0, 0, 0, 0, 1, 3));
    var character = new GameCharacter(0.1f, 120, 12, 30);
    character.setUser(user);
    user.setCharacter(character);
    character.prepareForFight();
    return user;
  }
}
