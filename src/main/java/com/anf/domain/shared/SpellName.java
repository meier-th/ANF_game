package com.anf.domain.shared;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpellName {
  PHYSICAL_ATTACK("Physical attack"),
  AIR_STRIKE("Air Strike"),
  FIRE_STRIKE("Fire Strike"),
  WATER_STRIKE("Water Strike"),
  BOSS_ATTACK("Boss attack");

  private final String value;

  public boolean matches(String candidate) {
    return value.equalsIgnoreCase(candidate);
  }
}
