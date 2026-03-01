package com.anf.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for core {@link GameCharacter} mechanics:
 * XP gain/level-up, HP/chakra changes, and fight preparation.
 */
public class CharacterTest {

    private GameCharacter character;
    private Stats stats;
    private User user;

    @Before
    public void setUp() {
        stats = new Stats(50, 0, 0, 0, 0, 0, 1, 0);
        user = new User();
        user.setStats(stats);
        character = new GameCharacter(0.1f, 200, 20, 50);
        character.setUser(user);
    }

    // --- changeXP ---

    @Test
    public void changeXP_doesNotLevelUpBelowThreshold() {
        character.changeXP(500);

        assertEquals(500, stats.getExperience());
        assertEquals(1, stats.getLevel());
        assertEquals(0, stats.getUpgradePoints());
    }

    @Test
    public void changeXP_grantsOneLevelAtExactThreshold() {
        character.changeXP(1000);

        assertEquals(1000, stats.getExperience());
        assertEquals(2, stats.getLevel());
        assertEquals(3, stats.getUpgradePoints());
    }

    @Test
    public void changeXP_grantsOneLevelWhenCrossingThreshold() {
        character.changeXP(900);  // brings to 900
        character.changeXP(300);  // crosses 1000

        assertEquals(1200, stats.getExperience());
        assertEquals(2, stats.getLevel());
        assertEquals(3, stats.getUpgradePoints());
    }

    @Test
    public void changeXP_grantsThreeLevelsAndNinePoints_onLargeGain() {
        character.changeXP(3500);

        assertEquals(3500, stats.getExperience());
        assertEquals(4, stats.getLevel());
        assertEquals(9, stats.getUpgradePoints());
    }

    @Test
    public void changeXP_accumulatesAcrossMultipleCalls() {
        character.changeXP(700);
        character.changeXP(700);

        assertEquals(1400, stats.getExperience());
        assertEquals(2, stats.getLevel());
        assertEquals(3, stats.getUpgradePoints());
    }

    // --- acceptDamage ---

    @Test
    public void acceptDamage_decreasesCurrentHP() {
        character.prepareForFight();
        int before = character.getCurrentHP();

        character.acceptDamage(50);

        assertEquals(before - 50, character.getCurrentHP());
    }

    @Test
    public void acceptDamage_canReduceHPBelowZero() {
        character.prepareForFight();
        character.acceptDamage(300);

        assertTrue(character.getCurrentHP() < 0);
    }

    // --- spendChakra ---

    @Test
    public void spendChakra_decreasesCurrentChakra() {
        character.prepareForFight();
        int before = character.getCurrentChakra();

        character.spendChakra(20);

        assertEquals(before - 20, character.getCurrentChakra());
    }

    // --- prepareForFight ---

    @Test
    public void prepareForFight_resetsHPAndChakraToMax() {
        character.acceptDamage(100);
        character.spendChakra(30);

        character.prepareForFight();

        assertEquals(character.getMaxHp(), character.getCurrentHP());
        assertEquals(character.getMaxChakra(), character.getCurrentChakra());
    }

    // helpers (JUnit 4 doesn't have assertFalse/assertTrue with message as first arg)
    private static void assertTrue(boolean condition) {
        org.junit.Assert.assertTrue(condition);
    }
}
