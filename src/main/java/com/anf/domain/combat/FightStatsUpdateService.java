package com.anf.domain.combat;

import com.anf.domain.shared.GameplayConstants;
import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.Boss;
import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.Stats;
import com.anf.domain.fight.FightVsAIService;
import com.anf.domain.fight.PVPFightsService;
import com.anf.domain.fight.UserAIFightService;
import com.anf.domain.user.CharacterService;
import com.anf.domain.user.StatsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class FightStatsUpdateService {
  private final StatsService statsService;
  private final PVPFightsService pvpFightsService;
  private final FightVsAIService fightVsAIService;
  private final UserAIFightService userAiFightService;
  private final CharacterService characterService;

  public void finalizePvpFight(FightPVP fight, boolean firstWon) {
    fight.setFirstWon(firstWon);
    var firstStats = fight.getFighter1().getStats();
    var secondStats = fight.getFighter2().getStats();
    var firstFighterPreviousRating = fight.getFighter1().getStats().getRating();
    var secondFighterPreviousRating = fight.getFighter2().getStats().getRating();
    var rating =
        firstFighterPreviousRating >= secondFighterPreviousRating && fight.isFirstWon()
                || secondFighterPreviousRating >= firstFighterPreviousRating && !fight.isFirstWon()
            ? fight.getLessRatingChange()
            : fight.getBiggerRatingChange();
    fight.setRatingChange(rating);
    if (fight.isFirstWon()) {
      firstStats.setRating(firstStats.getRating() + rating);
      firstStats.setFights(firstStats.getFights() + 1);
      firstStats.setWins(firstStats.getWins() + 1);
      secondStats.setRating(secondStats.getRating() - rating);
      secondStats.setFights(secondStats.getFights() + 1);
      secondStats.setLosses(secondStats.getLosses() + 1);
      secondStats.setDeaths(secondStats.getDeaths() + 1);
    } else {
      firstStats.setRating(firstStats.getRating() - rating);
      firstStats.setFights(firstStats.getFights() + 1);
      firstStats.setLosses(firstStats.getLosses() + 1);
      firstStats.setDeaths(firstStats.getDeaths() + 1);
      secondStats.setRating(secondStats.getRating() + rating);
      secondStats.setFights(secondStats.getFights() + 1);
      secondStats.setWins(secondStats.getWins() + 1);
    }
    applyExperience(
        firstStats,
        fight.isFirstWon()
            ? GameplayConstants.PVP_WIN_EXPERIENCE
            : GameplayConstants.PVP_LOSS_EXPERIENCE);
    applyExperience(
        secondStats,
        fight.isFirstWon()
            ? GameplayConstants.PVP_LOSS_EXPERIENCE
            : GameplayConstants.PVP_WIN_EXPERIENCE);

    statsService.addStats(firstStats);
    statsService.addStats(secondStats);
    fight.setFirstFighter(fight.getFighter1().getCharacter());
    fight.setSecondFighter(fight.getFighter2().getCharacter());
    pvpFightsService.addFight(fight);
  }

  public void finalizePveBossKilled(FightVsAI fight, Boss boss) {
    fightVsAIService.addFight(fight);
    for (var fightData : fight.getSetFighters()) {
      if (fightData.getResult() == null) {
        var fighter = fightData.getFighter();
        var fighterDied = fighter != null && fighter.getCurrentHP() <= 0;
        fightData.setResult(fighterDied ? AiFightParticipation.Result.DIED : AiFightParticipation.Result.WON);
      }
      var experience =
          GameplayConstants.PVE_BASE_EXPERIENCE
              + GameplayConstants.PVE_EXPERIENCE_PER_TAIL * boss.getNumberOfTails();
      var fighterStats = resolveStats(fightData);
      if (fighterStats == null) {
        log.warn("Skipping PvE boss-kill stats update due to missing fighter stats for participation {}", fightData.getId());
        fightData.setExperience(0);
        userAiFightService.add(fightData);
        continue;
      }
      if (fightData.getResult().equals(AiFightParticipation.Result.DIED)) {
        experience /= 2;
        fighterStats.setFights(fighterStats.getFights() + 1);
        fighterStats.setDeaths(fighterStats.getDeaths() + 1);
      } else {
        fighterStats.setFights(fighterStats.getFights() + 1);
        fighterStats.setWins(fighterStats.getWins() + 1);
      }
      applyExperience(fighterStats, experience);
      statsService.addStats(fighterStats);
      fightData.setExperience(experience);
      userAiFightService.add(fightData);
    }
  }

  public void finalizePvePlayersDefeated(FightVsAI fight) {
    fightVsAIService.addFight(fight);
    for (var userData : fight.getSetFighters()) {
      userData.setExperience(GameplayConstants.PVE_DEFEAT_EXPERIENCE);
      userData.setResult(AiFightParticipation.Result.LOST);
      var fighterStats = resolveStats(userData);
      if (fighterStats != null) {
        fighterStats.setFights(fighterStats.getFights() + 1);
        fighterStats.setLosses(fighterStats.getLosses() + 1);
        fighterStats.setDeaths(fighterStats.getDeaths() + 1);
        applyExperience(fighterStats, GameplayConstants.PVE_DEFEAT_EXPERIENCE);
        statsService.addStats(fighterStats);
      } else {
        log.warn("Skipping PvE defeat stats update due to missing fighter stats for participation {}", userData.getId());
        userData.setExperience(0);
      }
      userAiFightService.add(userData);
    }
  }

  private Stats resolveStats(AiFightParticipation participation) {
    if (participation == null || participation.getFighter() == null) {
      return null;
    }
    var fighter = participation.getFighter();
    if (fighter.getUser() != null && fighter.getUser().getStats() != null) {
      return fighter.getUser().getStats();
    }
    if (fighter.getId() > 0) {
      var persistedFighter = characterService.getCharacter(fighter.getId());
      if (persistedFighter != null && persistedFighter.getUser() != null && persistedFighter.getUser().getStats() != null) {
        participation.setFighter(persistedFighter);
        return persistedFighter.getUser().getStats();
      }
    }
    return null;
  }

  private void applyExperience(Stats stats, int experienceGain) {
    int previousXP = stats.getExperience();
    int newXP = previousXP + experienceGain;
    int levelsAcquired = (newXP - newXP % 1000 - (previousXP - previousXP % 1000)) / 1000;
    stats.setExperience(newXP);
    if (levelsAcquired > 0) {
      stats.setLevel(stats.getLevel() + levelsAcquired);
      stats.setUpgradePoints(stats.getUpgradePoints() + levelsAcquired * 3);
    }
  }
}
