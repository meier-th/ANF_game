package com.anf.service;

import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.Boss;
import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
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
    var firstFighterPreviousRating = fight.getFighter1().getStats().getRating();
    var secondFighterPreviousRating = fight.getFighter2().getStats().getRating();
    var rating =
        firstFighterPreviousRating >= secondFighterPreviousRating && fight.isFirstWon()
                || secondFighterPreviousRating >= firstFighterPreviousRating && !fight.isFirstWon()
            ? fight.getLessRatingChange()
            : fight.getBiggerRatingChange();
    fight.setRatingChange(rating);
    if (fight.isFirstWon()) {
      fight.getFighter1().getStats().setRating(fight.getFighter1().getStats().getRating() + rating);
      fight.getFighter1().getStats().setFights(fight.getFighter1().getStats().getFights() + 1);
      fight.getFighter1().getStats().setWins(fight.getFighter1().getStats().getWins() + 1);
      fight.getFighter2().getStats().setRating(fight.getFighter2().getStats().getRating() - rating);
      fight.getFighter2().getStats().setFights(fight.getFighter2().getStats().getFights() + 1);
      fight.getFighter2().getStats().setLosses(fight.getFighter2().getStats().getLosses() + 1);
    } else {
      fight.getFighter1().getStats().setRating(fight.getFighter1().getStats().getRating() - rating);
      fight.getFighter1().getStats().setFights(fight.getFighter1().getStats().getFights() + 1);
      fight.getFighter1().getStats().setLosses(fight.getFighter1().getStats().getLosses() + 1);
      fight.getFighter2().getStats().setRating(fight.getFighter2().getStats().getRating() + rating);
      fight.getFighter2().getStats().setFights(fight.getFighter2().getStats().getFights() + 1);
      fight.getFighter2().getStats().setWins(fight.getFighter2().getStats().getWins() + 1);
    }
    statsService.addStats(fight.getFighter1().getStats());
    statsService.addStats(fight.getFighter2().getStats());
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
      var experience = 500 + 200 * boss.getNumberOfTails();
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
      userData.setExperience(50);
      userData.setResult(AiFightParticipation.Result.LOST);
      userData.getFighter().getUser().getStats().setFights(userData.getFighter().getUser().getStats().getFights() + 1);
      userData.getFighter().getUser().getStats().setLosses(userData.getFighter().getUser().getStats().getLosses() + 1);
      userData.getFighter().changeXP(50);
      statsService.addStats(userData.getFighter().getUser().getStats());
      userAiFightService.add(userData);
    }
  }
}
