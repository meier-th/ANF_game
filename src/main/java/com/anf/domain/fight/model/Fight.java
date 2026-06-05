package com.anf.domain.fight.model;

import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;

/** Represents statistics during a fight */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Fight {

  public Fight() {
    synchronized (Fight.class) {
      number++;
    }
    fighters = new ArrayList<>();
    this.id = number;
    animals1 = new ArrayList<>();
    animals2 = new ArrayList<>();
  }

  protected final int id;

  private static volatile int number;

  private int currentAttacker = -1;

  protected ArrayList<User> fighters;

  protected User fighter1;

  protected User fighter2;

  protected ArrayList<NinjaAnimal> animals1;

  protected ArrayList<NinjaAnimal> animals2;

  protected String currentName = "";

  protected long timeLeft;

  public long getTimeLeft() {
    return timeLeft;
  }

  public void setTimeLeft(long timeLeft) {
    this.timeLeft = timeLeft;
  }

  public int getId() {
    return id;
  }

  public static int getNumber() {
    return number;
  }

  public static void setNumber(int number) {
    Fight.number = number;
  }

  public void addFighter(GameCharacter character) {
    fighters.add(character.getUser());
  }

  public void switchAttacker() {
    if (this instanceof FightPVP) {
      var turnSlots = 0;
      if (fighter1 != null) {
        turnSlots++;
      }
      if (fighter2 != null) {
        turnSlots++;
      }
      if (!animals1.isEmpty()) {
        turnSlots++;
      }
      if (!animals2.isEmpty()) {
        turnSlots++;
      }
      if (turnSlots == 0) {
        currentAttacker = -1;
        currentName = "";
        return;
      }
      currentAttacker = Math.floorMod(currentAttacker + 1, turnSlots);
    } else {
      currentAttacker = (currentAttacker + 1) % (fighters.size() + animals1.size() + 1);
      // 0-4 fighters[i], 5 - boss, 6-10 - animals1[i]
    }
    currentName = getCurrentAttacker(0);
  }

  public String getCurrentAttacker(int offset) {
    if (this instanceof FightPVP) {
      var order = new ArrayList<String>();
      if (fighter1 != null) {
        order.add(fighter1.getLogin());
      }
      if (fighter2 != null) {
        order.add(fighter2.getLogin());
      }
      if (!animals1.isEmpty()) {
        order.add(animals1.get(0).getName().substring(0, 3) + '1');
      }
      if (!animals2.isEmpty()) {
        order.add(animals2.get(0).getName().substring(0, 3) + '0');
      }
      if (order.isEmpty()) {
        return "";
      }
      return order.get(normalizedTurnIndex(order.size(), offset));
    } else {
      var turnSlots = 1 + animals1.size() + fighters.size();
      var attackerIndex = normalizedTurnIndex(turnSlots, offset);
      if (attackerIndex < fighters.size()) {
        return fighters.get(attackerIndex).getLogin();
      }
      if (attackerIndex == fighters.size()) {
        return String.valueOf(((FightVsAI) this).getBoss().getNumberOfTails());
      } else {
        return animals1.get(attackerIndex - fighters.size() - 1).getName().substring(0, 3);
      }
    }
  }

  @JsonIgnore
  public String getNextAttacker() {
    return getCurrentAttacker(1);
  }

  public ArrayList<User> getFighters() {
    return fighters;
  }

  public ArrayList<NinjaAnimal> getAnimals1() {
    return animals1;
  }

  public ArrayList<NinjaAnimal> getAnimals2() {
    return animals2;
  }

  public int getCurrentAttackerIndex() {
    return currentAttacker;
  }

  public void setCurrentAttackerIndex(int currentAttacker) {
    this.currentAttacker = currentAttacker;
  }

  private int normalizedTurnIndex(int turnSlots, int offset) {
    if (turnSlots <= 0) {
      return 0;
    }
    var baseIndex = currentAttacker < 0 ? 0 : currentAttacker;
    return Math.floorMod(baseIndex + offset, turnSlots);
  }
}
