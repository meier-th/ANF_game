package com.anf.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for turn rotation in {@link FightPVP}.
 *
 * <p>The turn order for a bare 1-v-1 (no summoned animals) is: 0 → fighter1, 1 → fighter2, then
 * wraps back.
 */
public class FightPvpTurnTest {

  private FightPVP fight;
  private User fighter1;
  private User fighter2;

  @Before
  public void setUp() {
    fight = new FightPVP();
    fighter1 = makeUser("alice");
    fighter2 = makeUser("bob");

    GameCharacter char1 = new GameCharacter(0.1f, 100, 10, 30);
    char1.setUser(fighter1);
    fighter1.setCharacter(char1);

    GameCharacter char2 = new GameCharacter(0.1f, 100, 10, 30);
    char2.setUser(fighter2);
    fighter2.setCharacter(char2);

    fight.setFighters(char1, char2);
  }

  @Test
  public void afterFirstSwitch_currentAttackerIsFighter1() {
    fight.switchAttacker();

    assertEquals("alice", fight.getCurrentAttacker(0));
  }

  @Test
  public void afterSecondSwitch_currentAttackerIsFighter2() {
    fight.switchAttacker();
    fight.switchAttacker();

    assertEquals("bob", fight.getCurrentAttacker(0));
  }

  @Test
  public void turnWrapsAround_afterBothFightersTookTurn() {
    fight.switchAttacker(); // alice
    fight.switchAttacker(); // bob
    fight.switchAttacker(); // back to alice

    assertEquals("alice", fight.getCurrentAttacker(0));
  }

  @Test
  public void getNextAttacker_returnsOpponentDuringAliceTurn() {
    fight.switchAttacker(); // alice's turn

    assertEquals("bob", fight.getNextAttacker());
  }

  @Test
  public void currentName_updatedAfterEachSwitch() {
    fight.switchAttacker();
    assertNotNull(fight.getCurrentAttacker(0));

    String first = fight.getCurrentAttacker(0);
    fight.switchAttacker();
    String second = fight.getCurrentAttacker(0);

    // must alternate
    assertNotNull(second);
    org.junit.Assert.assertNotEquals(first, second);
  }

  // helper
  private User makeUser(String login) {
    User u = new User();
    u.setLogin(login);
    Stats s = new Stats(50, 0, 0, 0, 0, 0, 1, 0);
    u.setStats(s);
    return u;
  }
}
