package com.anf.domain.combat;

import com.anf.domain.shared.ErrorCode;
import com.anf.domain.shared.SpellName;
import com.anf.domain.fight.model.Attack;
import com.anf.model.database.Boss;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.Spell;
import com.anf.model.database.SpellKnowledge;
import com.anf.model.database.User;
import com.anf.domain.fight.FightSnapshotService;
import com.anf.domain.fight.FightStateNotifier;
import com.anf.domain.user.UserService;
import com.anf.infrastructure.state.FightRuntimeStore;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class PveAttackService {
  private final UserService userService;
  private final SpellService spellService;
  private final SpellKnowledgeService spellKnowledgeService;
  private final FightRuntimeStore fightStateStore;
  private final FightSnapshotService fightSnapshotService;
  private final FightStateNotifier fightStateNotifier;
  private final FightDamageService fightDamageService;
  private final FightStatsUpdateService fightStatsUpdateService;

  public Attack attackPve(String attackerName, String fightUuid, String spellName) {
    log.debug("PvE attack requested for fight {} by {}", fightUuid, attackerName);
    Attack attack = new Attack();
    FightVsAI fight = (FightVsAI) fightStateStore.getFight(fightUuid).orElse(null);
    if (fight == null) {
      attack.setCode(ErrorCode.NOT_FOUND.getValue());
      return attack;
    }
    // Boss is a target
    Boss boss = fight.getBoss();
    User attacker = userService.getUser(attackerName);
    int damage;
    int chakra;
    // count damage
    if (!SpellName.PHYSICAL_ATTACK.matches(spellName)) {
      Spell spell = spellService.get(spellName);
      if (spell == null) {
        attack.setCode(ErrorCode.INVALID_REQUEST.getValue());
        return attack;
      }
      SpellKnowledge handling = spellKnowledgeService.getSpellKnowledge(attacker.getCharacter(), spell);
      damage =
          fightDamageService.computeSpellDamage(
              spellName, spell, handling, boss.getResistance());
      chakra = fightDamageService.computeChakraCost(spell, handling);
    } else {
      damage =
          fightDamageService.computePhysicalDamage(
              attacker.getCharacter().getPhysicalDamage(), boss.getResistance());
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
      fightStatsUpdateService.finalizePveBossKilled(fight, boss);
      // close fight
      fight.getFighters().forEach((fighter) -> fightStateStore.unmarkUserInFight(fighter.getLogin()));
      fightSnapshotService.deleteFightArtifacts(fightUuid, () -> fightStateStore.removeFight(fightUuid));
      fightFinished = true;
    }
    if (!fightFinished) {
      fightStateStore.saveFight(fightUuid, fight);
    }
    return attack;
  }

}
