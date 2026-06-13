package com.anf.domain.combat;

import com.anf.domain.shared.GameplayConstants;
import com.anf.domain.shared.SpellName;
import com.anf.model.database.Spell;
import com.anf.model.database.SpellKnowledge;
import org.springframework.stereotype.Service;

@Service
public class FightDamageService {

  public int computeSpellDamage(String spellName, Spell spell, SpellKnowledge handling, float targetResistance) {
    var baseDamage = spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel();
    var damage =
        SpellName.AIR_STRIKE.matches(spellName)
            ? baseDamage
            : Math.round(baseDamage * (1 - targetResistance));
    if (SpellName.FIRE_STRIKE.matches(spellName)
        && targetResistance < GameplayConstants.FIRE_STRIKE_DOUBLE_DAMAGE_RESISTANCE_CAP) {
      damage *= 2;
    }
    return damage;
  }

  public int computePhysicalDamage(int physicalDamage, float targetResistance) {
    return Math.round(physicalDamage * (1 - targetResistance));
  }

  public int computeChakraCost(Spell spell, SpellKnowledge handling) {
    return Math.max(
        0,
        spell.getBaseChakraConsumption()
            - handling.getSpellLevel() * spell.getChakraConsumptionPerLevel());
  }

  public int computeBossAttackDamage(int tails, float targetResistance) {
    return (int)
        Math.round(
            GameplayConstants.BOSS_ATTACK_BASE_DAMAGE
                * Math.pow(tails, GameplayConstants.BOSS_ATTACK_TAILS_POWER)
                * (1 - targetResistance));
  }
}
