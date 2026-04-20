package com.anf.domain.shared;

import com.anf.domain.fight.model.NinjaAnimalRace;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NinjaAnimalDefinition {
  VESELIBA_TIER_1("Vertet", NinjaAnimalRace.Veseliba, "Ver", 10, 200, 1),
  VESELIBA_TIER_2("Ubele", NinjaAnimalRace.Veseliba, "Ube", 35, 500, 10),
  BOJAJUMUS_TIER_1("Lauva", NinjaAnimalRace.Bojajumus, "Lau", 30, 50, 1),
  BOJAJUMUS_TIER_2("Lusis", NinjaAnimalRace.Bojajumus, "Lus", 120, 150, 10),
  LIDZSVARU_TIER_1("Erglis", NinjaAnimalRace.Lidzsvaru, "Erg", 20, 100, 1),
  LIDZSVARU_TIER_2("Lapsa", NinjaAnimalRace.Lidzsvaru, "Lap", 70, 250, 10),
  BUGURT_TIER_1("Тётя Срака", NinjaAnimalRace.Bugurt, "Тёт", 18, 250, 1),
  BUGURT_TIER_2("Дядя Бафомет", NinjaAnimalRace.Bugurt, "Дяд", 55, 370, 10);

  private final String name;
  private final NinjaAnimalRace race;
  private final String attackerToken;
  private final int damage;
  private final int maxHp;
  private final int requiredLevel;

  public static NinjaAnimalDefinition forRaceAndLevel(NinjaAnimalRace race, boolean lvl2) {
    return Arrays.stream(values())
        .filter((candidate) -> candidate.race == race)
        .filter(
            (candidate) ->
                lvl2
                    ? candidate.requiredLevel >= GameplayConstants.SUMMON_LEVEL_TWO_THRESHOLD
                    : candidate.requiredLevel < GameplayConstants.SUMMON_LEVEL_TWO_THRESHOLD)
        .findFirst()
        .orElse(LIDZSVARU_TIER_1);
  }

  public static NinjaAnimalDefinition byAttackerToken(String attackerToken) {
    return Arrays.stream(values())
        .filter((candidate) -> candidate.attackerToken.equals(attackerToken))
        .findFirst()
        .orElse(LIDZSVARU_TIER_1);
  }
}
