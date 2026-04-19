package com.anf.service;

import com.anf.service.fight.model.NinjaAnimal;
import com.anf.service.fight.model.NinjaAnimalRace;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NinjaAnimalResolverService {
  private final NinjaAnimalService ninjaAnimalService;

  public String animalNameForRace(NinjaAnimalRace race, boolean lvl2) {
    return switch (race) {
      case Bugurt -> lvl2 ? "Дядя Бафомет" : "Тётя Срака";
      case Veseliba -> lvl2 ? "Ubele" : "Vertet";
      case Bojajumus -> lvl2 ? "Lusis" : "Lauva";
      default -> lvl2 ? "Lapsa" : "Erglis";
    };
  }

  public NinjaAnimal resolveByPvePvpAttackerToken(String attackerToken) {
    return switch (attackerToken) {
      case "Дяд" -> ninjaAnimalService.findByName("Дядя Бафомет");
      case "Тёт" -> ninjaAnimalService.findByName("Тётя Срака");
      case "Ube" -> ninjaAnimalService.findByName("Ubele");
      case "Ver" -> ninjaAnimalService.findByName("Vertet");
      case "Lus" -> ninjaAnimalService.findByName("Lusis");
      case "Lau" -> ninjaAnimalService.findByName("Lauva");
      case "Lap" -> ninjaAnimalService.findByName("Lapsa");
      default -> ninjaAnimalService.findByName("Erglis");
    };
  }

  public NinjaAnimal resolveByAnimalName(String animalName) {
    return ninjaAnimalService.findByName(animalName);
  }
}
