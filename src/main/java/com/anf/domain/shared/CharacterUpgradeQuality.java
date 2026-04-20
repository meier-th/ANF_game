package com.anf.domain.shared;

import java.util.Arrays;

public enum CharacterUpgradeQuality {
  DAMAGE("damage"),
  HP("hp"),
  RESISTANCE("resistance"),
  CHAKRA("chakra");

  private final String requestValue;

  CharacterUpgradeQuality(String requestValue) {
    this.requestValue = requestValue;
  }

  public static CharacterUpgradeQuality fromRequest(String quality) {
    return Arrays.stream(values())
        .filter((candidate) -> candidate.requestValue.equalsIgnoreCase(quality))
        .findFirst()
        .orElse(null);
  }
}
