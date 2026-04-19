package com.anf.service;

import com.anf.model.NinjaAnimal;
import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.Boss;
import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.User;
import com.anf.service.state.FightRuntimeStore;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AnimalTurnService {
  private final FightRuntimeStore fightStateStore;
  private final FightSnapshotService fightSnapshotService;
  private final FightStateNotifier fightStateNotifier;
  private final PVPFightsService pvpFightsService;
  private final FightVsAIService fightVsAIService;
  private final UserAIFightService userAiFightService;
  private final StatsService statsService;
  private final NinjaAnimalResolverService ninjaAnimalResolverService;

  public void handleAnimalPvpAttack(FightPVP fight, String fightUuid, Runnable scheduleNextTurn) {
    String animName = fight.getCurrentAttacker(0).substring(0, 3);
    boolean fromAnimals1 = animName.charAt(3) == '1';
    NinjaAnimal attacker = ninjaAnimalResolverService.resolveByPvePvpAttackerToken(animName);
    User target;
    NinjaAnimal targetAnimal;
    boolean targetUser;
    if (!fromAnimals1) {
      int targetNum = (int) Math.round(Math.random() * fight.getAnimals1().size());
      targetUser = targetNum == 0;
      target = targetUser ? fight.getFighter1() : null;
      targetAnimal = targetUser ? null : fight.getAnimals1().get(targetNum - fight.getAnimals1().size());
    } else {
      int targetNum = (int) Math.round(Math.random() * fight.getAnimals2().size());
      targetUser = targetNum == 0;
      target = targetUser ? fight.getFighter2() : null;
      targetAnimal = targetUser ? null : fight.getAnimals2().get(targetNum - fight.getAnimals2().size());
    }
    boolean deadly = false;
    int damage = attacker.getDamage();
    if (targetUser) {
      damage *= (1 - target.getCharacter().getResistance());
      target.getCharacter().acceptDamage(damage);
      if (target.getCharacter().getCurrentHP() <= 0) deadly = true;
    } else {
      damage *= (1 - targetAnimal.getResistance());
      targetAnimal.acceptDamage(damage);
      if (targetAnimal.getCurrentHP() <= 0) deadly = true;
    }
    boolean finish = targetUser && deadly;
    fightStateNotifier.sendAfterAttack(
        fight.getFighter1().getLogin(),
        damage,
        targetUser ? target.getLogin() : targetAnimal.getName(),
        fight.getCurrentAttacker(0),
        fight.getNextAttacker(),
        deadly,
        finish,
        "Physical attack",
        0,
        0);
    fightStateNotifier.sendAfterAttack(
        fight.getFighter2().getLogin(),
        damage,
        targetUser ? target.getLogin() : targetAnimal.getName(),
        fight.getCurrentAttacker(0),
        fight.getNextAttacker(),
        deadly,
        finish,
        "Physical attack",
        0,
        0);
    if (deadly && !finish) {
      if (fromAnimals1) {
        fight.getAnimals2().clear();
        System.out.println("animal2 died");
      } else {
        fight.getAnimals1().clear();
        System.out.println("animal1 died");
      }
    }
    if (finish) {
      if (fromAnimals1) {
        fight.setFirstWon(true);
      } else {
        fight.setFirstWon(false);
      }
      int firstFighterPreviousRating = fight.getFighter1().getStats().getRating();
      int secondFighterPreviousRating = fight.getFighter2().getStats().getRating();
      int rating;
      if (firstFighterPreviousRating >= secondFighterPreviousRating && fight.isFirstWon()
          || secondFighterPreviousRating >= firstFighterPreviousRating && !fight.isFirstWon()) {
        rating = fight.getLessRatingChange();
      } else {
        rating = fight.getBiggerRatingChange();
      }
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
      fightSnapshotService.deleteFightArtifacts(fightUuid, () -> fightStateStore.removeFight(fightUuid));
      fightStateStore.unmarkUserInFight(fight.getFighter1().getLogin());
      fightStateStore.unmarkUserInFight(fight.getFighter2().getLogin());
      return;
    }
    fightStateStore.saveFight(fightUuid, fight);
    scheduleNextTurn.run();
  }

  public void handleAnimalPveAttack(FightVsAI fight, String fightUuid, Runnable scheduleNextTurn) {
    System.out.println("Animal attack");
    String animName = fight.getCurrentAttacker(0);
    NinjaAnimal attacker = ninjaAnimalResolverService.resolveByPvePvpAttackerToken(animName);
    Boss target = fight.getBoss();
    int damage = Math.round(attacker.getDamage() * (1 - target.getResistance()));
    target.acceptDamage(damage);
    boolean deadly = false;
    if (target.getCurrentHP() <= 0) deadly = true;
    final boolean dead = deadly;
    fight
        .getSetFighters()
        .forEach(
            (set) ->
                fightStateNotifier.sendAfterAttack(
                    set.getFighter().getUser().getLogin(),
                    damage,
                    String.valueOf(target.getNumberOfTails()),
                    fight.getCurrentAttacker(0),
                    fight.getNextAttacker(),
                    dead,
                    dead,
                    "Physical attack",
                    0,
                    0));
    if (deadly) {
      fightVsAIService.addFight(fight);
      for (AiFightParticipation fightData : fight.getSetFighters()) {
        if (fightData.getResult() == null) fightData.setResult(AiFightParticipation.Result.WON);
        int experience = 500 + 200 * target.getNumberOfTails();
        if (fightData.getResult().equals(AiFightParticipation.Result.DIED)) {
          experience /= 2;
          fightData.getFighter().getUser().getStats().setFights(fightData.getFighter().getUser().getStats().getFights() + 1);
          fightData.getFighter().getUser().getStats().setDeaths(fightData.getFighter().getUser().getStats().getDeaths() + 1);
          fightData.getFighter().changeXP(experience);
          statsService.addStats(fightData.getFighter().getUser().getStats());
        } else {
          fightData.getFighter().getUser().getStats().setFights(fightData.getFighter().getUser().getStats().getFights() + 1);
          fightData.getFighter().getUser().getStats().setWins(fightData.getFighter().getUser().getStats().getWins() + 1);
          fightData.getFighter().changeXP(experience);
          statsService.addStats(fightData.getFighter().getUser().getStats());
        }
        fightData.setExperience(experience);
        userAiFightService.add(fightData);
      }
      for (AiFightParticipation fighter : fight.getSetFighters()) {
        fightStateStore.unmarkUserInFight(fighter.getFighter().getUser().getLogin());
      }
      fightSnapshotService.deleteFightArtifacts(fightUuid, () -> fightStateStore.removeFight(fightUuid));
      return;
    }
    fightStateStore.saveFight(fightUuid, fight);
    scheduleNextTurn.run();
  }
}
