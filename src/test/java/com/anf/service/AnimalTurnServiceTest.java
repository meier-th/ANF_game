package com.anf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.model.NinjaAnimal;
import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.Boss;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Stats;
import com.anf.model.database.User;
import com.anf.service.state.FightRuntimeStore;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnimalTurnServiceTest {
  private FightRuntimeStore fightStateStore;
  private FightSnapshotService fightSnapshotService;
  private FightStateNotifier fightStateNotifier;
  private PVPFightsService pvpFightsService;
  private FightVsAIService fightVsAIService;
  private UserAIFightService userAiFightService;
  private StatsService statsService;
  private NinjaAnimalResolverService ninjaAnimalResolverService;
  private AnimalTurnService animalTurnService;

  @BeforeEach
  void setUp() {
    fightStateStore = mock(FightRuntimeStore.class);
    fightSnapshotService = mock(FightSnapshotService.class);
    fightStateNotifier = mock(FightStateNotifier.class);
    pvpFightsService = mock(PVPFightsService.class);
    fightVsAIService = mock(FightVsAIService.class);
    userAiFightService = mock(UserAIFightService.class);
    statsService = mock(StatsService.class);
    ninjaAnimalResolverService = mock(NinjaAnimalResolverService.class);
    animalTurnService =
        new AnimalTurnService(
            fightStateStore,
            fightSnapshotService,
            fightStateNotifier,
            pvpFightsService,
            fightVsAIService,
            userAiFightService,
            statsService,
            ninjaAnimalResolverService);
  }

  @Test
  void handleAnimalPveAttack_savesFightAndSchedulesNextTurn_whenFightContinues() {
    var fight = new FightVsAI();
    var user = user("alice");
    fight.addFighter(user.getCharacter());
    fight.setSetFighters(new ArrayList<>(java.util.List.of(participation(fight, user))));
    var boss = mock(Boss.class);
    when(boss.getResistance()).thenReturn(0.1f);
    when(boss.getCurrentHP()).thenReturn(1000);
    when(boss.getNumberOfTails()).thenReturn(1);
    fight.setBoss(boss);
    fight.switchAttacker();

    var attacker = mock(NinjaAnimal.class);
    when(attacker.getDamage()).thenReturn(20);
    when(ninjaAnimalResolverService.resolveByPvePvpAttackerToken(any())).thenReturn(attacker);

    var nextTurnCalled = new AtomicBoolean(false);
    animalTurnService.handleAnimalPveAttack(fight, "fight-1", () -> nextTurnCalled.set(true));

    assertThat(nextTurnCalled.get()).isTrue();
    verify(fightStateStore).saveFight("fight-1", fight);
  }

  private User user(String login) {
    var user = new User();
    user.setLogin(login);
    user.setStats(new Stats(100, 0, 0, 0, 0, 0, 1, 3));
    var character = new GameCharacter(0.2f, 120, 10, 30);
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
