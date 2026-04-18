package com.anf.service;

import com.anf.model.Attack;
import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.Boss;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.Spell;
import com.anf.model.database.SpellKnowledge;
import com.anf.model.database.User;
import com.anf.service.state.LegacyFightRuntimeStore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PveAttackService {
  private final UserService userService;
  private final SpellService spellService;
  private final SpellKnowledgeService spellKnowledgeService;
  private final FightVsAIService fightVsAIService;
  private final UserAIFightService userAiFightService;
  private final StatsService statsService;
  private final LegacyFightRuntimeStore fightStateStore;
  private final FightSnapshotService fightSnapshotService;
  private final FightStateNotifier fightStateNotifier;

  public Attack attackPve(String attackerName, String fightUuid, String spellName) {
    System.out.println("AttackPve at" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
    Attack attack = new Attack();
    FightVsAI fight = (FightVsAI) fightStateStore.getFight(fightUuid).orElse(null);
    if (fight == null) {
      attack.setCode(2);
      return attack;
    }
    // Boss is a target
    Boss boss = fight.getBoss();
    User attacker = userService.getUser(attackerName);
    int damage;
    int chakra;
    // count damage
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
                    * (1 - boss.getResistance()));
      }
      if (spellName.equals("Fire Strike") && boss.getResistance() < 0.8) {
        damage *= 2;
      }
      chakra =
          spell.getBaseChakraConsumption()
              + handling.getSpellLevel() * spell.getChakraConsumptionPerLevel();
    } else {
      damage = Math.round(attacker.getCharacter().getPhysicalDamage() * (1 - boss.getResistance()));
      chakra = 0;
    }
    attack.setDamage(damage);
    attack.setChakra(chakra);
    attacker.getCharacter().spendChakra(chakra);
    boss.acceptDamage(damage);
    attack.setDeadly(boss.getCurrentHP() <= 0);
    List<User> fighters = fight.getFighters();
    String nextAtt = fight.getNextAttacker();
    for (User fighter : fighters) {
      fightStateNotifier.sendAfterAttack(
          fighter.getLogin(),
          damage,
          String.valueOf(boss.getNumberOfTails()),
          attacker.getLogin(),
          nextAtt,
          attack.isDeadly(),
          attack.isDeadly(),
          spellName,
          chakra,
          0);
    }
    // if boss was killed
    boolean fightFinished = false;
    if (attack.isDeadly()) {
      fightVsAIService.addFight(fight);
      // set stats and save
      for (AiFightParticipation fightData : fight.getSetFighters()) {
        if (fightData.getResult() == null) fightData.setResult(AiFightParticipation.Result.WON);
        int experience = 500 + 200 * boss.getNumberOfTails();
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
          fightData.getFighter().changeXP(experience);
          statsService.addStats(fightData.getFighter().getUser().getStats());
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
          fightData.getFighter().changeXP(experience);
          statsService.addStats(fightData.getFighter().getUser().getStats());
        }
        fightData.setExperience(experience);
        userAiFightService.add(fightData);
      }
      // close fight
      for (AiFightParticipation fighter : fight.getSetFighters()) {
        fightStateStore.unmarkUserInFight(fighter.getFighter().getUser().getLogin());
      }
      fightSnapshotService.deleteFightArtifacts(fightUuid, () -> fightStateStore.removeFight(fightUuid));
      fightFinished = true;
    }
    if (!fightFinished) {
      fightStateStore.saveFight(fightUuid, fight);
    }
    return attack;
  }

}
