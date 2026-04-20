package com.anf.domain.shared;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GameplayConstants {
  public static final int SUMMON_LEVEL_TWO_THRESHOLD = 10;

  public static final int PVP_BASE_RATING_CHANGE = 15;
  public static final int PVP_BIGGER_RATING_GAP_DIVISOR = 4;
  public static final int PVP_LESSER_RATING_GAP_DIVISOR = 8;
  public static final int PVP_MIN_RATING_CHANGE = 5;

  public static final int DAMAGE_UPGRADE_INCREMENT = 4;
  public static final int HP_UPGRADE_INCREMENT = 15;
  public static final int CHAKRA_UPGRADE_INCREMENT = 7;
  public static final int RESISTANCE_GROWTH_DIVISOR = 4;

  public static final float NO_RESISTANCE = 0.0f;
  public static final float FIRE_STRIKE_DOUBLE_DAMAGE_RESISTANCE_CAP = 0.8f;

  public static final int WATER_STRIKE_CHAKRA_BURN_DIVISOR = 10;
  public static final int BOSS_ATTACK_BASE_DAMAGE = 30;
  public static final double BOSS_ATTACK_TAILS_POWER = 1.5;

  public static final int PVE_BASE_EXPERIENCE = 500;
  public static final int PVE_EXPERIENCE_PER_TAIL = 200;
  public static final int PVE_DEFEAT_EXPERIENCE = 50;

  public static final int PVP_ANIMAL_TOKEN_LENGTH = 4;
  public static final int PVE_BOSS_TOKEN_MAX_LENGTH = 2;
  public static final int PVE_ANIMAL_TOKEN_MIN_LENGTH = 3;
  public static final int PVE_ANIMAL_TOKEN_MAX_LENGTH = 4;
  public static final int ANIMAL_SLOT_MARKER_INDEX = 3;
  public static final char ANIMAL_SLOT_ONE = '1';
}
