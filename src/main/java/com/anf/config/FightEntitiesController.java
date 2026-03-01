package com.anf.config;

import com.anf.model.NinjaAnimal;
import com.anf.model.NinjaAnimalRace;
import com.anf.model.database.Boss;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Spell;
import com.anf.model.database.SpellKnowledge;
import com.anf.model.database.Stats;
import com.anf.model.database.User;
import com.anf.service.BossService;
import com.anf.service.CharacterService;
import com.anf.service.SpellKnowledgeService;
import com.anf.service.SpellService;
import com.anf.service.StatsService;
import com.anf.service.UserService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class FightEntitiesController {

  private final BossService bossService;
  private final SpellKnowledgeService spellHandService;
  private final UserService userService;
  private final SpellService spelService;
  private final StatsService statsService;
  private final CharacterService charService;

  @GetMapping("/fight/boss")
  public ResponseEntity<?> getBoss(@RequestParam int id) {
    try {
      Boss boss = bossService.getBoss(id);
      if (boss == null)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Boss with id = " + id + " doesn't exist.");
      return ResponseEntity.status(HttpStatus.OK).body(boss);
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @GetMapping("/fight/spell")
  public ResponseEntity<?> getAllSpells() {
    try {
      Iterable<Spell> spells = spelService.getAllSpells();
      return ResponseEntity.ok(spells);
    } catch (Throwable exc) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("\"" + exc.getMessage() + "\"");
    }
  }

  @GetMapping("/fight/spell/my/all")
  public ResponseEntity<?> getAvailableSpellKnowledges() {
    try {
      User user =
          userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
      GameCharacter ch = user.getCharacter();
      List<SpellKnowledge> SpellKnowledges = spellHandService.getPersonsHandling(ch);
      return ResponseEntity.status(HttpStatus.OK).body(SpellKnowledges);
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @GetMapping("/fight/spell/my")
  public ResponseEntity<?> getMySpellKnowledge(@RequestParam String spellname) {
    try {
      User user =
          userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
      GameCharacter ch = user.getCharacter();
      Spell spell = spelService.get(spellname);
      SpellKnowledge spellHandl = spellHandService.getSpellKnowledge(ch, spell);
      if (spellHandl == null)
        return ResponseEntity.status(HttpStatus.LOCKED).body("User can't handle this spell yet.");
      return ResponseEntity.status(HttpStatus.OK).body(spellHandl);
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @PostMapping("/fight/spell/my")
  public ResponseEntity<String> acquireSpellKnowledge(@RequestParam String spellname) {
    try {
      User user =
          userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
      if (user.getStats().getUpgradePoints() == 0)
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User doesn't have upgrade points");
      GameCharacter ch = user.getCharacter();
      int currLvl = 0;
      Spell spell = spelService.get(spellname);
      if (spellHandService.getSpellKnowledge(ch, spell) != null) {
        SpellKnowledge handl = spellHandService.getSpellKnowledge(ch, spell);
        currLvl = handl.getSpellLevel();
        handl.setSpellLevel(currLvl + 1);
        spellHandService.addOrUpdateHandling(handl);
      } else {
        spellHandService.addOrUpdateHandling(new SpellKnowledge(currLvl + 1, spell, ch));
      }
      Stats stats = user.getStats();
      stats.setUpgradePoints(stats.getUpgradePoints() - 1);
      statsService.addStats(stats);
      return ResponseEntity.status(HttpStatus.CREATED).body("Spell handling is updated.");
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @GetMapping("/fight/animals/my")
  public ResponseEntity<?> getAvailableAnimals() {
    try {
      User user =
          userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
      GameCharacter character = user.getCharacter();
      if (character == null)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No character was found.");
      final int lvl = character.getUser().getStats().getLevel();
      NinjaAnimalRace race = character.getAnimalRace();
      if (race == null)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("User doesn't have any animal race connected to his character.");

      for (NinjaAnimal anim : NinjaAnimal.animals) {
        if (anim.getRace().equals(race) && lvl >= 10 && anim.getLevel() == 10
            || lvl < 10 && anim.getRace().equals(race) && anim.getLevel() == 1) {
          return ResponseEntity.status(HttpStatus.OK).body(anim);
        }
      }
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("\"Animal not found\"");
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @GetMapping("/fight/animals")
  public ResponseEntity<?> getAllAnimals() {
    try {
      List<NinjaAnimal> animals = NinjaAnimal.animals;
      return ResponseEntity.status(HttpStatus.OK).body(animals);
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @PostMapping("/fight/animals/my")
  public ResponseEntity<String> setMyAnimalRace(@RequestParam String racename) {
    try {
      User user =
          userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
      GameCharacter ch = user.getCharacter();
      if (ch.getAnimalRace() != null)
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body("\"User can not change his character's ninja animal's race.\"");
      try {
        NinjaAnimalRace.valueOf(racename);
      } catch (IllegalArgumentException exc) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("\"Such a race doesn't exist.\"");
      }
      NinjaAnimalRace race = NinjaAnimalRace.valueOf(racename);
      ch.setAnimalRace(race);
      charService.addCharacter(ch);
      return ResponseEntity.status(HttpStatus.CREATED).body("\"Animal race is set for user.\"");
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("\"" + error.getMessage() + "\"");
    }
  }
}
