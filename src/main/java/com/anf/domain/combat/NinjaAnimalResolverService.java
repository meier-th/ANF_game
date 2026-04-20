package com.anf.domain.combat;

import com.anf.domain.fight.model.NinjaAnimal;
import com.anf.domain.fight.model.NinjaAnimalRace;
import com.anf.domain.shared.NinjaAnimalDefinition;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NinjaAnimalResolverService {
  private final NinjaAnimalService ninjaAnimalService;

  public String animalNameForRace(NinjaAnimalRace race, boolean lvl2) {
    return NinjaAnimalDefinition.forRaceAndLevel(race, lvl2).getName();
  }

  public NinjaAnimal resolveByPvePvpAttackerToken(String attackerToken) {
    return ninjaAnimalService.findByName(NinjaAnimalDefinition.byAttackerToken(attackerToken).getName());
  }

  public NinjaAnimal resolveByAnimalName(String animalName) {
    return ninjaAnimalService.findByName(animalName);
  }
}
