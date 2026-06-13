package com.anf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.domain.combat.FightStatsUpdateService;
import com.anf.domain.combat.SpellKnowledgeService;
import com.anf.domain.fight.FightVsAIService;
import com.anf.domain.fight.PVPFightsService;
import com.anf.domain.fight.UserAIFightService;
import com.anf.domain.shared.GameplayConstants;
import com.anf.domain.user.CharacterService;
import com.anf.domain.user.StatsService;
import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.Boss;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Stats;
import com.anf.model.database.User;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FightStatsUpdateServiceTest {
  private StatsService statsService;
  private FightVsAIService fightVsAIService;
  private UserAIFightService userAiFightService;
  private CharacterService characterService;
  private FightStatsUpdateService service;

  @BeforeEach
  void setUp() {
    statsService = mock(StatsService.class);
    fightVsAIService = mock(FightVsAIService.class);
    userAiFightService = mock(UserAIFightService.class);
    characterService = mock(CharacterService.class);
    service =
        new FightStatsUpdateService(
            statsService,
            mock(PVPFightsService.class),
            fightVsAIService,
            userAiFightService,
            characterService,
            mock(SpellKnowledgeService.class));
  }

  @Test
  void finalizePvePlayersDefeated_resolvesStatsFromCharacter_whenRuntimeUserMissing() {
    var fight = new FightVsAI();
    var participation = new AiFightParticipation();
    var detachedFighter = new GameCharacter(0.1f, 200, 10, 30);
    detachedFighter.setId(42);
    participation.setFight(fight);
    participation.setFighter(detachedFighter);
    fight.setSetFighters(new ArrayList<>(java.util.List.of(participation)));

    var persistedStats = new Stats(100, 0, 0, 0, 0, 0, 1, 3);
    var persistedUser = new User();
    persistedUser.setStats(persistedStats);
    var persistedCharacter = new GameCharacter(0.1f, 200, 10, 30);
    persistedCharacter.setId(42);
    persistedCharacter.setUser(persistedUser);
    when(characterService.getCharacter(42)).thenReturn(persistedCharacter);

    service.finalizePvePlayersDefeated(fight);

    assertThat(participation.getResult()).isEqualTo(AiFightParticipation.Result.LOST);
    assertThat(participation.getExperience()).isEqualTo(GameplayConstants.PVE_DEFEAT_EXPERIENCE);
    assertThat(persistedStats.getFights()).isEqualTo(1);
    assertThat(persistedStats.getLosses()).isEqualTo(1);
    assertThat(persistedStats.getDeaths()).isEqualTo(1);
    assertThat(persistedStats.getExperience()).isEqualTo(GameplayConstants.PVE_DEFEAT_EXPERIENCE);
    verify(fightVsAIService).addFight(fight);
    verify(statsService).addStats(persistedStats);
    verify(userAiFightService).add(participation);
  }

  @Test
  void finalizePveBossKilled_marksZeroHpParticipantAsDied_whenResultUnset() {
    var fight = new FightVsAI();
    var participation = new AiFightParticipation();
    var fighter = new GameCharacter(0.1f, 200, 10, 30);
    fighter.setCurrentHP(0);
    var stats = new Stats(0, 0, 0, 0, 0, 0, 1, 0);
    var user = new User();
    user.setStats(stats);
    fighter.setUser(user);
    participation.setFight(fight);
    participation.setFighter(fighter);
    fight.setSetFighters(new ArrayList<>(java.util.List.of(participation)));
    var boss = new Boss("Shukaku", 1, 100);

    service.finalizePveBossKilled(fight, boss);

    assertThat(participation.getResult()).isEqualTo(AiFightParticipation.Result.DIED);
    var expectedExperience =
        (GameplayConstants.PVE_BASE_EXPERIENCE
                + GameplayConstants.PVE_EXPERIENCE_PER_TAIL * boss.getNumberOfTails())
            / 2;
    assertThat(participation.getExperience()).isEqualTo(expectedExperience);
    assertThat(stats.getDeaths()).isEqualTo(1);
    verify(userAiFightService).add(participation);
  }
}
