package com.p3212.Configurations;

import com.p3212.EntityClasses.Boss;
import com.p3212.EntityClasses.Character;
import com.p3212.EntityClasses.NinjaAnimal;
import com.p3212.EntityClasses.Spell;
import com.p3212.EntityClasses.SpellHandling;
import com.p3212.EntityClasses.Stats;
import com.p3212.EntityClasses.User;
import com.p3212.EntityClasses.NinjaAnimalRace;
import com.p3212.Repositories.NinjaAnimalRaceRepository;
import com.p3212.Services.BossService;
import com.p3212.Services.CharacterService;
import com.p3212.Services.NinjaAnimalService;
import com.p3212.Services.SpellHandlingService;
import com.p3212.Services.SpellService;
import com.p3212.Services.StatsService;
import com.p3212.Services.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FightEntitiesController {
	
	@Autowired
	private BossService bossServ;
	
	@Autowired
	private SpellHandlingService spellHandServ;
        
        @Autowired
        private NinjaAnimalRaceRepository raceRepository;
	
	@Autowired
	private UserService userServ;
	
	@Autowired
	private SpellService spelServ;
	
        @Autowired
        private NinjaAnimalService ninjaAnimalServ;
        
        @Autowired
        private StatsService statsServ;
        
        @Autowired
        private CharacterService charServ;
        
	@GetMapping("/fight/boss")
	public ResponseEntity<?> getBoss(@RequestParam int id) {
		try {
			Boss boss = bossServ.getBoss(id);
                        if (boss == null)
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Boss with id = "+id+" doesn't exist.");
			return ResponseEntity.status(HttpStatus.OK).body(boss);
		} catch (Throwable error) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
		}
	}
	
	@GetMapping("/fight/spell")
	public ResponseEntity<?> getSpell(@RequestParam int id) {
		try {
			Spell spell = spelServ.get(id);
                        if (spell == null)
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Spell with id = "+id+" doesn't exist");
			return ResponseEntity.status(HttpStatus.OK).body(spell);
		} catch (Throwable error) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
		}
	}
	
	@GetMapping("/fight/spell/my/all")
        public ResponseEntity<?> getAvailableSpellHandlings() {
		try {
			User user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
			Character ch = user.getCharacter();
			List<SpellHandling> spellHandlings = spellHandServ.getPersonsHandling(ch);
			return ResponseEntity.status(HttpStatus.OK).body(spellHandlings);
		} catch (Throwable error) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
		}
	}
	
        @GetMapping("/fight/spell/my")
        public ResponseEntity<?> getMySpellHandling(@RequestBody Spell spell) {
            try {
                User user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
                Character ch = user.getCharacter();
                SpellHandling spellHandl = spellHandServ.getSpellHandling(ch, spell);
                if (spellHandl == null)
                    return ResponseEntity.status(HttpStatus.LOCKED).body("User can't handle this spell yet.");
                return ResponseEntity.status(HttpStatus.OK).body(spellHandl);
            } catch (Throwable error) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
            }
        }
        
        @PostMapping("/fight/spell/my")
        public ResponseEntity<String> acquireSpellHandling(@RequestBody Spell spell) {
            try {
                User user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
                if (user.getStats().getUpgradePoints() == 0)
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User doesn't have upgrade points");
                Character ch = user.getCharacter();
                int currLvl = 0;
                if (spellHandServ.getSpellHandling(ch, spell) != null) {
                    SpellHandling handl = spellHandServ.getSpellHandling(ch, spell);
                    currLvl = handl.getSpellLevel();
                    handl.setSpellLevel(currLvl + 1);
                    spellHandServ.addOrUpdateHandling(handl);
                } else {
                    spellHandServ.addOrUpdateHandling(new SpellHandling(currLvl + 1, spell, ch));
                }
                Stats stats = user.getStats();
                stats.setUpgradePoints(stats.getUpgradePoints() - 1);
                statsServ.addStats(stats);
                return ResponseEntity.status(HttpStatus.CREATED).body("Spell handling is updated.");
            } catch (Throwable error) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
            }
        }
        
        @GetMapping("/fight/animals/my")
    public ResponseEntity<?> getAvailableAnimals() {
        try {
            User user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            Character character = user.getCharacter();
            if (character == null) 
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No character was found.");
            final int lvl = character.getUser().getStats().getLevel();
            NinjaAnimalRace race = character.getAnimalRace();
            if (race == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User doesn't have any animal race connected to his character.");
            NinjaAnimal animal = new NinjaAnimal();
            for (NinjaAnimal anim: NinjaAnimal.animals) {
                if (anim.getRace().equals(race) && lvl >= 10 && anim.getLevel() == 10 || lvl < 10 && anim.getRace().equals(race) && anim.getLevel() == 1) {
                    animal = anim;
                    break;
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(animal);
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
            User user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            Character ch = user.getCharacter();
            if (ch.getAnimalRace() != null)
               return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User can not change his character's ninja animal's race.");
            try {
            NinjaAnimalRace.races raceOP = NinjaAnimalRace.races.valueOf(racename);
            } catch (IllegalArgumentException exc) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Such a race doesn't exist.");
            }
            NinjaAnimalRace race = new NinjaAnimalRace(NinjaAnimalRace.races.valueOf(racename).toString());
            ch.setAnimalRace(race);
            charServ.addCharacter(ch);
            return ResponseEntity.status(HttpStatus.CREATED).body("Animal race is set for user.");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
        }
    }
    
    @GetMapping("/fight/animals/{racename}/all")
    public ResponseEntity<?> getRaceAnimals(@PathVariable String racename) {
        try {
            NinjaAnimalRace race = new NinjaAnimalRace(racename);
            Object[] animals = NinjaAnimal.animals.stream().filter(animal -> animal.getRace().equals(race)).toArray();
            return ResponseEntity.status(HttpStatus.OK).body(animals);
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }
    
}
