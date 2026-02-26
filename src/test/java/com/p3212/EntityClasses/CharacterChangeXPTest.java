package com.p3212.EntityClasses;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Smoke tests for {@link Character#changeXP(int)} to ensure that
 * level and upgrade points are derived from experience as expected.
 */
public class CharacterChangeXPTest {

    @Test
    public void changeXpAwardsLevelsAndUpgradePointsPerThousandXp() {
        Stats stats = new Stats(50, 0, 0, 0, 0, 900, 1, 0);
        User user = new User();
        user.setStats(stats);

        Character character = new Character(0.1f, 100, 10, 30);
        character.setUser(user);

        character.changeXP(300);

        // 900 + 300 = 1200 XP => +1 level, +3 upgrade points
        assertEquals(1200, user.getStats().getExperience());
        assertEquals(2, user.getStats().getLevel());
        assertEquals(3, user.getStats().getUpgradePoints());
    }

    @Test
    public void changeXpCanAwardMultipleLevelsAtOnce() {
        Stats stats = new Stats(50, 0, 0, 0, 0, 100, 1, 0);
        User user = new User();
        user.setStats(stats);

        Character character = new Character(0.1f, 100, 10, 30);
        character.setUser(user);

        character.changeXP(2100);

        // 100 + 2100 = 2200 XP => +2 levels, +6 upgrade points
        assertEquals(2200, user.getStats().getExperience());
        assertEquals(3, user.getStats().getLevel());
        assertEquals(6, user.getStats().getUpgradePoints());
    }
}

