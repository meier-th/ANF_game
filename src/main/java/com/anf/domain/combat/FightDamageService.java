package com.anf.domain.combat;

import com.anf.model.database.Spell;
import com.anf.model.database.SpellKnowledge;
import org.springframework.stereotype.Service;

@Service
public class FightDamageService {

  public int computeSpellDamage(String spellName, Spell spell, SpellKnowledge handling, float targetResistance) {
    var baseDamage = spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel();
    var damage = "Air Strike".equals(spellName) ? baseDamage : Math.round(baseDamage * (1 - targetResistance));
    if ("Fire Strike".equals(spellName) && targetResistance < 0.8f) {
      damage *= 2;
    }
    return damage;
  }

  public int computePhysicalDamage(int physicalDamage, float targetResistance) {
    return Math.round(physicalDamage * (1 - targetResistance));
  }

  public int computeChakraCost(Spell spell, SpellKnowledge handling) {
    return spell.getBaseChakraConsumption()
        + handling.getSpellLevel() * spell.getChakraConsumptionPerLevel();
  }

  public int computeBossAttackDamage(int tails, float targetResistance) {
    return (int) Math.round(30 * Math.pow(tails, 1.5) * (1 - targetResistance));
  }
}
