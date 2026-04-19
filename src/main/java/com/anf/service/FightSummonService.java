package com.anf.service;

import com.anf.configuration.WebSocketsController;
import com.anf.service.fight.model.NinjaAnimal;
import com.anf.service.fight.model.NinjaAnimalRace;
import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.User;
import com.anf.infrastructure.state.FightRuntimeStore;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FightSummonService {
  private final UserService userService;
  private final NinjaAnimalResolverService ninjaAnimalResolverService;
  private final FightRuntimeStore fightStateStore;
  private final WebSocketsController webSocketsController;

  public ResponseEntity<?> summonPvp(String fightUuid, String username) {
    User user = userService.getUser(username);
    FightPVP fight = (FightPVP) fightStateStore.getFight(fightUuid).orElse(null);
    if (fight == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\"code\": 2,\"error\":\"Fight doesn't exist\"}");
    }
    NinjaAnimalRace race = user.getCharacter().getAnimalRace();
    if (race == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("{\"answer\":\"You haven't chosen your animal\"}");
    }

    boolean lvl2 = user.getCharacter().getLevel() >= 10;
    String animalName = ninjaAnimalResolverService.animalNameForRace(race, lvl2);
    NinjaAnimal animal = ninjaAnimalResolverService.resolveByAnimalName(animalName);
    animal.prepareForFight();
    if (fight.getFighter1().getLogin().equals(username)) {
      fight.getAnimals1().add(animal);
      webSocketsController.sendSummon(fight.getFighter2().getLogin(), username, animal, animalName);
    } else {
      fight.getAnimals2().add(animal);
      webSocketsController.sendSummon(fight.getFighter1().getLogin(), username, animal, animalName);
    }
    fightStateStore.saveFight(fightUuid, fight);
    return ResponseEntity.ok(animal.toString());
  }

  public ResponseEntity<?> summonPve(String fightUuid, String username) {
    User user = userService.getUser(username);
    FightVsAI fight = (FightVsAI) fightStateStore.getFight(fightUuid).orElse(null);
    if (fight == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\"code\": 2,\"error\":\"Fight doesn't exist\"}");
    }
    NinjaAnimalRace race = user.getCharacter().getAnimalRace();
    boolean lvl2 = user.getCharacter().getLevel() >= 10;
    String animalName = ninjaAnimalResolverService.animalNameForRace(race, lvl2);
    NinjaAnimal animal = ninjaAnimalResolverService.resolveByAnimalName(animalName);
    animal.prepareForFight();
    fight.getAnimals1().add(animal);
    fight
        .getSetFighters()
        .forEach(
            (set) -> {
              if (!set.getFighter().getUser().getLogin().equals(username)) {
                webSocketsController.sendSummon(
                    set.getFighter().getUser().getLogin(), username, animal, animalName);
              }
            });
    fightStateStore.saveFight(fightUuid, fight);
    return ResponseEntity.ok(animal.toString());
  }

}
