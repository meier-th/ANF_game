package com.anf.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Spell#performAttack(int, float)}.
 *
 * <p>This covers the core damage/chakra calculation that drives every fight action. Formula: damage
 * = (baseDamage + level * damagePerLevel) * (1 - resistance) chakra = baseChakraConsumption - level
 * * chakraConsumptionPerLevel
 */
public class SpellTest {

  @Test
  public void performAttack_calculatesCorrectDamageWithNoResistance() {
    // baseDamage=50, +10 per level, level=3, resistance=0 → (50+30)*1 = 80
    Spell spell = new Spell("Fireball", 50, 10, 1, 20, 2);

    Attack attack = spell.performAttack(3, 0.0f);

    assertEquals(80, attack.getDamage());
  }

  @Test
  public void performAttack_reducedDamageByResistance() {
    // (50+30)*(1-0.25) = 80*0.75 = 60
    Spell spell = new Spell("Fireball", 50, 10, 1, 20, 2);

    Attack attack = spell.performAttack(3, 0.25f);

    assertEquals(60, attack.getDamage());
  }

  @Test
  public void performAttack_calculatesCorrectChakraCost() {
    // chakra = 20 - 3*2 = 14
    Spell spell = new Spell("Fireball", 50, 10, 1, 20, 2);

    Attack attack = spell.performAttack(3, 0.0f);

    assertEquals(14, attack.getChakra());
  }

  @Test
  public void performAttack_chakraCostDecreasesWithLevel() {
    Spell spell = new Spell("Wave", 30, 5, 1, 30, 3);

    Attack lowLevel = spell.performAttack(1, 0.0f);
    Attack highLevel = spell.performAttack(5, 0.0f);

    assertTrue(
        highLevel.getChakra() < lowLevel.getChakra(),
        "Higher level should cost less chakra");
  }

  @Test
  public void performAttack_baseLevelZero_givesBaseDamageAndBaseCost() {
    Spell spell = new Spell("Scratch", 20, 5, 1, 10, 1);

    Attack attack = spell.performAttack(0, 0.0f);

    assertEquals(20, attack.getDamage());
    assertEquals(10, attack.getChakra());
  }

  @Test
  public void performAttack_fullResistanceSetsZeroDamage() {
    Spell spell = new Spell("Bolt", 100, 10, 1, 20, 2);

    Attack attack = spell.performAttack(3, 1.0f);

    assertEquals(0, attack.getDamage());
  }
}
