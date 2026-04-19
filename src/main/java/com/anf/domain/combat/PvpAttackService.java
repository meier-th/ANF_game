package com.anf.domain.combat;

import com.anf.domain.fight.model.Attack;
import com.anf.model.database.FightPVP;
import com.anf.model.database.Spell;
import com.anf.model.database.SpellKnowledge;
import com.anf.model.database.User;
import com.anf.domain.fight.FightSnapshotService;
import com.anf.domain.fight.FightStateNotifier;
import com.anf.infrastructure.state.FightRuntimeStore;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class PvpAttackService {
  private final SpellService spellService;
  private final SpellKnowledgeService spellKnowledgeService;
  private final FightRuntimeStore fightStateStore;
  private final FightSnapshotService fightSnapshotService;
  private final FightStateNotifier fightStateNotifier;
  private final FightDamageService fightDamageService;
  private final FightStatsUpdateService fightStatsUpdateService;

  public Attack attackPvp(String attackerName, String enemyName, String fightUuid, String spellName) {
    Attack attack = new Attack();
    FightPVP fight = (FightPVP) fightStateStore.getFight(fightUuid).orElse(null);
    if (fight == null) {
      attack.setCode(2);
      return attack;
    }
    User attacker = fight.getFighter1();
    User enemy = fight.getFighter2();
    com.anf.domain.fight.model.NinjaAnimal targetAnimal = null;
    boolean userIsTarget = true;
    if (enemyName.length() < 6) {
      userIsTarget = false;
      if (enemyName.charAt(3) == 0) targetAnimal = fight.getAnimals2().get(0);
      else targetAnimal = fight.getAnimals1().get(0);
    }
    if (attacker == null || enemy == null) {
      attack.setCode(6);
      return attack;
    }
    if (userIsTarget && enemy.getLogin().equals(attackerName)) {
      User tmp = attacker;
      attacker = enemy;
      enemy = tmp;
    }
    int damage;
    int chakra;
    if (userIsTarget)
      if (!spellName.equalsIgnoreCase("Physical attack")) {
        Spell spell = spellService.get(spellName);
        if (spell == null) {
          attack.setCode(8);
          return attack;
        }
        SpellKnowledge handling = spellKnowledgeService.getSpellKnowledge(attacker.getCharacter(), spell);
        damage =
            fightDamageService.computeSpellDamage(
                spellName, spell, handling, enemy.getCharacter().getResistance());
        chakra = fightDamageService.computeChakraCost(spell, handling);
      } else {
        damage =
            fightDamageService.computePhysicalDamage(
                attacker.getCharacter().getPhysicalDamage(), enemy.getCharacter().getResistance());
        chakra = 0;
      }
    // animal is a target
    else {
      if (!spellName.equalsIgnoreCase("Physical attack")) {
        Spell spell = spellService.get(spellName);
        if (spell == null) {
          attack.setCode(8);
          return attack;
        }
        SpellKnowledge handling = spellKnowledgeService.getSpellKnowledge(attacker.getCharacter(), spell);
        damage =
            fightDamageService.computeSpellDamage(
                spellName, spell, handling, targetAnimal.getResistance());
        chakra = fightDamageService.computeChakraCost(spell, handling);
      } else {
        damage =
            fightDamageService.computePhysicalDamage(
                attacker.getCharacter().getPhysicalDamage(), targetAnimal.getResistance());
        chakra = 0;
      }
    }
    int chakraBurn = 0;
    if (spellName.equals("Water Strike")) {
      chakraBurn = damage / 10;
    }
    attack.setDamage(damage);
    attack.setChakra(chakra);
    attacker.getCharacter().spendChakra(chakra);
    if (userIsTarget) {
      enemy.getCharacter().acceptDamage(damage);
      attack.setDeadly(enemy.getCharacter().getCurrentHP() <= 0);
      fightStateNotifier.sendAfterAttack(
          enemyName,
          damage,
          enemyName,
          attackerName,
          fight.getNextAttacker(),
          attack.isDeadly(),
          attack.isDeadly(),
          spellName,
          chakra,
          chakraBurn);
      fightStateNotifier.sendAfterAttack(
          attackerName,
          damage,
          enemyName,
          attackerName,
          fight.getNextAttacker(),
          attack.isDeadly(),
          attack.isDeadly(),
          spellName,
          chakra,
          chakraBurn);
    } else {
      targetAnimal.acceptDamage(damage);
      attack.setDeadly(targetAnimal.getCurrentHP() <= 0);
      fightStateNotifier.sendAfterAttack(
          fight.getFighter1().getLogin(),
          damage,
          targetAnimal.getName(),
          attackerName,
          fight.getNextAttacker(),
          attack.isDeadly(),
          false,
          spellName,
          chakra,
          0);
      fightStateNotifier.sendAfterAttack(
          fight.getFighter2().getLogin(),
          damage,
          targetAnimal.getName(),
          attackerName,
          fight.getNextAttacker(),
          attack.isDeadly(),
          false,
          spellName,
          chakra,
          0);
    }

    boolean fightFinished = false;
    if (attack.isDeadly()) {
      if (!userIsTarget) {
        if (enemyName.charAt(3) == 1) {
          fight.getAnimals1().clear();
          log.debug("Animal from slot 1 died in fight {}", fightUuid);
        } else {
          fight.getAnimals2().clear();
          log.debug("Animal from slot 2 died in fight {}", fightUuid);
        }
      } else {
        var firstWon = !fight.getFighter1().getLogin().equals(enemyName);
        fightStatsUpdateService.finalizePvpFight(fight, firstWon);
        fightSnapshotService.deleteFightArtifacts(fightUuid, () -> fightStateStore.removeFight(fightUuid));
        fightStateStore.unmarkUserInFight(attackerName);
        fightStateStore.unmarkUserInFight(enemyName);
        fightFinished = true;
      }
    }
    if (!fightFinished) {
      fightStateStore.saveFight(fightUuid, fight);
    }
    return attack;
  }

}
