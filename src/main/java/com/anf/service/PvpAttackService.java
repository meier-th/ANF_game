package com.anf.service;

import com.anf.model.Attack;
import com.anf.model.database.FightPVP;
import com.anf.model.database.Spell;
import com.anf.model.database.SpellKnowledge;
import com.anf.model.database.User;
import com.anf.service.state.LegacyFightRuntimeStore;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PvpAttackService {
  private final SpellService spellService;
  private final SpellKnowledgeService spellKnowledgeService;
  private final StatsService statsService;
  private final PVPFightsService pvpFightsService;
  private final LegacyFightRuntimeStore fightStateStore;
  private final InMemoryFightTurnScheduler fightTurnScheduler;
  private final FightSnapshotService fightSnapshotService;
  private final FightStateNotifier fightStateNotifier;

  public Attack attackPvp(String attackerName, String enemyName, String fightUuid, String spellName) {
    Attack attack = new Attack();
    FightPVP fight = (FightPVP) fightStateStore.getFight(fightUuid).orElse(null);
    if (fight == null) {
      attack.setCode(2);
      return attack;
    }
    User attacker = fight.getFighter1();
    User enemy = fight.getFighter2();
    com.anf.model.NinjaAnimal targetAnimal = null;
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
        SpellKnowledge handling = spellKnowledgeService.getSpellKnowledge(attacker.getCharacter(), spell);
        if (spell == null) {
          attack.setCode(8);
          return attack;
        }
        if (spellName.equals("Air Strike")) {
          damage = spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel();
        } else {
          damage =
              Math.round(
                  (spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel())
                      * (1 - enemy.getCharacter().getResistance()));
        }
        if (spellName.equals("Fire Strike") && enemy.getCharacter().getResistance() < 0.8) {
          damage *= 2;
        }
        chakra =
            spell.getBaseChakraConsumption()
                + handling.getSpellLevel() * spell.getChakraConsumptionPerLevel();
      } else {
        damage = Math.round(attacker.getCharacter().getPhysicalDamage() * (1 - enemy.getCharacter().getResistance()));
        chakra = 0;
      }
    // animal is a target
    else {
      if (!spellName.equalsIgnoreCase("Physical attack")) {
        Spell spell = spellService.get(spellName);
        SpellKnowledge handling = spellKnowledgeService.getSpellKnowledge(attacker.getCharacter(), spell);
        if (spell == null) {
          attack.setCode(8);
          return attack;
        }
        if (spellName.equals("Air Strike")) {
          damage = spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel();
        } else {
          damage =
              Math.round(
                  (spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel())
                      * (1 - targetAnimal.getResistance()));
        }
        if (spellName.equals("Fire Strike") && targetAnimal.getResistance() < 0.8) {
          damage *= 2;
        }
        chakra =
            spell.getBaseChakraConsumption()
                + handling.getSpellLevel() * spell.getChakraConsumptionPerLevel();
      } else {
        damage = Math.round(attacker.getCharacter().getPhysicalDamage() * (1 - targetAnimal.getResistance()));
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
          System.out.println("animal 1 died");
        } else {
          fight.getAnimals2().clear();
          System.out.println("animal 2 died");
        }
      } else {
        if (fight.getFighter1().getLogin().equals(enemyName)) {
          fight.setFirstWon(false);
        } else {
          fight.setFirstWon(true);
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
        fightTurnScheduler.cancel(fightUuid);
        fightTurnScheduler.remove(fightUuid);
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
