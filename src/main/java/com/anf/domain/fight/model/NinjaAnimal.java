package com.anf.domain.fight.model;

import com.anf.domain.shared.GameplayConstants;
import com.anf.domain.shared.NinjaAnimalDefinition;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.ArrayList;

public class NinjaAnimal extends Creature implements Serializable {

  public static final ArrayList<NinjaAnimal> animals = new ArrayList<>();

  static {
    for (var definition : NinjaAnimalDefinition.values()) {
      animals.add(
          new NinjaAnimal(
              definition.getName(),
              definition.getRace(),
              definition.getDamage(),
              definition.getMaxHp(),
              definition.getRequiredLevel()));
    }
  }

  private final String name;

  private final int requiredLevel;

  private final int maxHP;

  private final int damage;

  private String summoner;

  private final NinjaAnimalRace race;

  public String getSummoner() {
    return summoner;
  }

  public void setSummoner(String summoner) {
    this.summoner = summoner;
  }

  public String getName() {
    return name;
  }

  public int getRequiredLevel() {
    return requiredLevel;
  }

  public int getDamage() {
    return damage;
  }

  public NinjaAnimalRace getRace() {
    return race;
  }

  private NinjaAnimal(String name, NinjaAnimalRace race, int damage, int hp, int reqlevel) {
    this.damage = damage;
    this.maxHP = hp;
    this.name = name;
    this.race = race;
    this.requiredLevel = reqlevel;
  }

  @Override
  public void acceptDamage(int damage) {
    currentHP -= damage;
  }

  @Override
  public float getResistance() {
    return GameplayConstants.NO_RESISTANCE;
  }

  @Override
  public int getLevel() {
    return 1;
  }

  @Override
  public int getMaxHp() {
    return maxHP;
  }

  @Override
  public int getMaxChakra() {
    return 0;
  }

  @Override
  public String toString() {
    try {
      return new ObjectMapper().writeValueAsString(this);
    } catch (JacksonException e) {
      return "{}";
    }
  }
}
