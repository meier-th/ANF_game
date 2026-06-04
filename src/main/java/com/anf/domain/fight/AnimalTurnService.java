package com.anf.domain.fight;

import com.anf.domain.fight.model.NinjaAnimal;
import com.anf.domain.shared.SpellName;
import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.Boss;
import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.User;
import com.anf.domain.combat.FightStatsUpdateService;
import com.anf.domain.combat.NinjaAnimalResolverService;
import com.anf.infrastructure.state.FightRuntimeStore;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class AnimalTurnService {
  private final FightRuntimeStore fightStateStore;
  private final FightSnapshotService fightSnapshotService;
  private final FightStateNotifier fightStateNotifier;
  private final NinjaAnimalResolverService ninjaAnimalResolverService;
  private final FightStatsUpdateService fightStatsUpdateService;

  public void handleAnimalPvpAttack(FightPVP fight, String fightUuid, Runnable scheduleNextTurn) {
    String attackerToken = fight.getCurrentAttacker(0);
    if (attackerToken == null || attackerToken.length() < 3) {
      log.warn("Invalid animal attacker token '{}' for fight {}", attackerToken, fightUuid);
      scheduleNextTurn.run();
      return;
    }
    String animName = attackerToken.substring(0, 3);
    boolean fromAnimals1 = attackerToken.length() > 3 && attackerToken.charAt(3) == '1';
    NinjaAnimal attacker = ninjaAnimalResolverService.resolveByPvePvpAttackerToken(animName);
    if (attacker == null) {
      log.warn("Animal attacker '{}' could not be resolved in fight {}", attackerToken, fightUuid);
      scheduleNextTurn.run();
      return;
    }
    User target;
    NinjaAnimal targetAnimal;
    boolean targetUser;
    if (!fromAnimals1) {
      int targetNum = (int) Math.round(Math.random() * fight.getAnimals1().size());
      targetUser = targetNum == 0;
      target = targetUser ? fight.getFighter1() : null;
      targetAnimal = targetUser ? null : fight.getAnimals1().get(targetNum - 1);
    } else {
      int targetNum = (int) Math.round(Math.random() * fight.getAnimals2().size());
      targetUser = targetNum == 0;
      target = targetUser ? fight.getFighter2() : null;
      targetAnimal = targetUser ? null : fight.getAnimals2().get(targetNum - 1);
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
        SpellName.PHYSICAL_ATTACK.getValue(),
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
        SpellName.PHYSICAL_ATTACK.getValue(),
        0,
        0);
    if (deadly && !finish) {
      if (fromAnimals1) {
        fight.getAnimals2().clear();
        log.debug("Animal from side 2 died in fight {}", fightUuid);
      } else {
        fight.getAnimals1().clear();
        log.debug("Animal from side 1 died in fight {}", fightUuid);
      }
    }
    if (finish) {
      fightStatsUpdateService.finalizePvpFight(fight, fromAnimals1);
      fightSnapshotService.deleteFightArtifacts(fightUuid, () -> fightStateStore.removeFight(fightUuid));
      fightStateStore.unmarkUserInFight(fight.getFighter1().getLogin());
      fightStateStore.unmarkUserInFight(fight.getFighter2().getLogin());
      return;
    }
    fightStateStore.saveFight(fightUuid, fight);
    scheduleNextTurn.run();
  }

  public void handleAnimalPveAttack(FightVsAI fight, String fightUuid, Runnable scheduleNextTurn) {
    log.debug("Animal attack in fight {}", fightUuid);
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
                    SpellName.PHYSICAL_ATTACK.getValue(),
                    0,
                    0));
    if (deadly) {
      fightStatsUpdateService.finalizePveBossKilled(fight, target);
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
