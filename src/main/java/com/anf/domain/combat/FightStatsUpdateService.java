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
import com.anf.domain.user.StatsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FightStatsUpdateService {
  private final StatsService statsService;
  private final PVPFightsService pvpFightsService;
  private final FightVsAIService fightVsAIService;
  private final UserAIFightService userAiFightService;

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
        fightData.setResult(AiFightParticipation.Result.WON);
      }
      var experience =
          GameplayConstants.PVE_BASE_EXPERIENCE
              + GameplayConstants.PVE_EXPERIENCE_PER_TAIL * boss.getNumberOfTails();
      if (fightData.getResult().equals(AiFightParticipation.Result.DIED)) {
        experience /= 2;
        fightData
            .getFighter()
            .getUser()
            .getStats()
            .setFights(fightData.getFighter().getUser().getStats().getFights() + 1);
        fightData
            .getFighter()
            .getUser()
            .getStats()
            .setDeaths(fightData.getFighter().getUser().getStats().getDeaths() + 1);
      } else {
        fightData
            .getFighter()
            .getUser()
            .getStats()
            .setFights(fightData.getFighter().getUser().getStats().getFights() + 1);
        fightData
            .getFighter()
            .getUser()
            .getStats()
            .setWins(fightData.getFighter().getUser().getStats().getWins() + 1);
      }
      fightData.getFighter().changeXP(experience);
      statsService.addStats(fightData.getFighter().getUser().getStats());
      fightData.setExperience(experience);
      userAiFightService.add(fightData);
    }
  }

  public void finalizePvePlayersDefeated(FightVsAI fight) {
    fightVsAIService.addFight(fight);
    for (var userData : fight.getSetFighters()) {
      userData.setExperience(GameplayConstants.PVE_DEFEAT_EXPERIENCE);
      userData.setResult(AiFightParticipation.Result.LOST);
      userData.getFighter().getUser().getStats().setFights(userData.getFighter().getUser().getStats().getFights() + 1);
      userData.getFighter().getUser().getStats().setLosses(userData.getFighter().getUser().getStats().getLosses() + 1);
      userData.getFighter().getUser().getStats().setDeaths(userData.getFighter().getUser().getStats().getDeaths() + 1);
      userData.getFighter().changeXP(GameplayConstants.PVE_DEFEAT_EXPERIENCE);
      statsService.addStats(userData.getFighter().getUser().getStats());
      userAiFightService.add(userData);
    }
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
