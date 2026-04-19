package com.anf.service;

import com.anf.model.NinjaAnimal;
import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.User;
import com.anf.service.state.FightRuntimeStore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BossTurnService {
  private final FightRuntimeStore fightStateStore;
  private final FightSnapshotService fightSnapshotService;
  private final FightStateNotifier fightStateNotifier;
  private final FightVsAIService fightVsAIService;
  private final UserAIFightService userAiFightService;
  private final StatsService statsService;

  public void handleBossAttack(FightVsAI fight, String fightUuid, Runnable scheduleNextTurn) {
    int targetNum = (int) (Math.random() * (fight.getFighters().size() + fight.getAnimals1().size() - 0.5));
    boolean targetUser = targetNum < fight.getFighters().size();
    User target = targetUser ? fight.getFighters().get(targetNum) : null;
    NinjaAnimal targetAnimal = targetUser ? null : fight.getAnimals1().get(targetNum - fight.getFighters().size());

    int damage =
        (int)
            Math.round(
                30
                    * Math.pow(fight.getBoss().getNumberOfTails(), 1.5)
                    * (targetUser ? (1 - target.getCharacter().getResistance()) : (1 - targetAnimal.getResistance())));
    boolean deadly;
    if (targetUser) {
      target.getCharacter().acceptDamage(damage);
      System.out.println(
          "Boss Attack at"
              + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
              + "Damage: "
              + damage
              + "Current: "
              + target.getCharacter().getCurrentHP()
              + '\n');
      deadly = target.getCharacter().getCurrentHP() <= 0;
      if (deadly) {
        for (int i = 0; i < fight.getFighters().size(); i++) {
          if (fight.getFighters().get(i).getLogin().equals(target.getLogin())) {
            System.out.println("Killed: " + fight.getFighters().remove(i).getLogin() + '\n');
            break;
          }
        }
        fight
            .getSetFighters()
            .forEach(
                (set) -> {
                  if (set.getFighter().getUser().getLogin().equals(target.getLogin()))
                    set.setResult(AiFightParticipation.Result.DIED);
                });
        System.out.print("Remaining: ");
      }
      fight.getFighters().forEach((user) -> System.out.print(user.getLogin() + " "));
    } else {
      targetAnimal.acceptDamage(damage);
      deadly = targetAnimal.getCurrentHP() <= 0;
      if (deadly) {
        fight
            .getAnimals1()
            .forEach(
                (an) -> {
                  if (an.getName().equalsIgnoreCase(targetAnimal.getName())) fight.getAnimals1().remove(an);
                });
      }
    }
    boolean allDead = true;
    for (User user : fight.getFighters()) {
      if (user.getCharacter().getCurrentHP() > 0) allDead = false;
    }
    System.out.println("Are all dead: " + allDead + "\n");
    final boolean everyoneDied = allDead;

    fight
        .getSetFighters()
        .forEach(
            (fighter) ->
                fightStateNotifier.sendAfterAttack(
                    fighter.getFighter().getUser().getLogin(),
                    damage,
                    targetUser ? target.getLogin() : targetAnimal.getName().substring(0, 3),
                    fight.getCurrentAttacker(0),
                    fight.getNextAttacker(),
                    deadly,
                    everyoneDied,
                    "Boss attack",
                    0,
                    0));

    if (allDead) {
      System.out.println("fight ended\n");
      fightVsAIService.addFight(fight);
      for (AiFightParticipation userData : fight.getSetFighters()) {
        userData.setExperience(50);
        userData.setResult(AiFightParticipation.Result.LOST);
        userData.getFighter().getUser().getStats().setFights(userData.getFighter().getUser().getStats().getFights() + 1);
        userData.getFighter().getUser().getStats().setLosses(userData.getFighter().getUser().getStats().getLosses() + 1);
        userData.getFighter().changeXP(50);
        statsService.addStats(userData.getFighter().getUser().getStats());
        userAiFightService.add(userData);
      }
      for (AiFightParticipation fighter : fight.getSetFighters()) {
        fightStateStore.unmarkUserInFight(fighter.getFighter().getUser().getLogin());
      }
      fightSnapshotService.deleteFightArtifacts(fightUuid, () -> fightStateStore.removeFight(fightUuid));
      return;
    }
    System.out.println("YEEEE!\n");
    fightStateStore.saveFight(fightUuid, fight);
    scheduleNextTurn.run();
  }
}
