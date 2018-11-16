package com.p3212.configuration;

import com.p3212.EntityClasses.*;
import com.p3212.EntityClasses.Character;
import com.p3212.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/fight")
public class FightController {
    @Autowired
    UserService userService;

    @Autowired
    SpellService spellService;

    @Autowired
    BossService bossService;
    @Autowired
    PVPFightsService pvpFightsService;

    @Autowired
    FightVsAIService fightVsAIService;

    @Autowired
    NinjaAnimalService ninjaAnimalService;

    private HashMap<Integer, Fight> fights;

    {
        fights = new HashMap<>();
    }

    @RequestMapping("/startPvp")
    public String startPvp(@RequestParam(name = "fighter1") String fighter1Name, @RequestParam(name = "fighter2") String fighter2Name) {
        FightPVP fight = new FightPVP();
        Character fighter1 = userService.getUser(fighter1Name).getCharacter();
        Character fighter2 = userService.getUser(fighter2Name).getCharacter();
        if (fighter1 == null || fighter2 == null) return "{ \"code\": 3}"; //code 3 means fighter does't exist
        fighter1.prepareForFight();
        fighter2.prepareForFight();
        fight.addFighter(fighter1, 1);
        fight.addFighter(fighter2, 2);
        fight.setRatingChange(5); //TODO
        fights.put(fight.getId(), fight);
        return fight.toString();
    }

    @RequestMapping("/startPve")
    public String startPve(@RequestParam(name = "fighters") String[] fighters, @RequestParam(name = "bossId") int bossId) {
        FightVsAI fight = new FightVsAI();
        for (String fighterName : fighters) {
            Character fighter = userService.getUser(fighterName).getCharacter();
            fighter.prepareForFight();
            fight.addFighter(fighter, 1);
        }
        Boss boss = bossService.getBoss(bossId);
        boss.prepareForFight();
        fight.addFighter(boss, 2);
        fights.put(fight.getId(), fight);
        return fight.toString();
    }

    @RequestMapping("/attack")
    public String attack(@RequestParam(name = "attackerNumber") int attackerNumber,
                         @RequestParam(name = "enemyNumber") int enemyNumber,
                         @RequestParam(name = "fightId") int fightId,
                         @RequestParam(name = "spellId") int spellId) {
        Fight fight = fights.get(fightId);
        if (fight == null) return "{\n\"code\": 2\n}";              //code 2 means fight doesn't exist
        attackerNumber--;
        enemyNumber--;
        Creature attacker = fight.getFighters().get(attackerNumber).getValue();
        Creature enemy = fight.getFighters().get(enemyNumber).getValue();
        //TODO maybe check belonging
        Spell spell = spellService.get(spellId);
        Attack attack = spell.performAttack(attacker.getLevel(), enemy.getResistance());
        if (attacker.getCurrentChakra() < attack.getChakra())
            return "{\n\"code\": 1\n}";                             //code 1 means attacker doesn't have enough chakra
        enemy.acceptDamage(attack.getDamage());
        if (enemy.getCurrentHP() <= 0) {
            attack.setDeadly(true);
            if (enemy instanceof NinjaAnimal) {
                fight.getFighters().remove(enemyNumber); //TODO I've kinda forgot smth
                return attack.toString();
            }
            if (fight instanceof FightPVP) {
                ((FightPVP) fight).setFighters((Character) attacker, (Character) enemy);
                ((FightPVP) fight).setFirstWon(
                        fights.get(fightId)
                                .getFighters()
                                .get(enemyNumber)
                                .getKey() == 1);
                stopFight(fight);
                ((Character) attacker).getUser().getStats().changeRating(((FightPVP) fight).getRatingChange());
                ((Character) enemy).getUser().getStats().changeRating(-((FightPVP) fight).getRatingChange());
                fights.remove(fightId);
            } else {
                if (fight.getFighters().get(enemyNumber).getKey() == 1) {
                    fight.getFighters().remove(enemyNumber);
                    if (fight.getFighters().size() < 2) stopFight(fight);               //ВСЕ SASNOOLEY
                    return attack.toString();
                } else {
                    stopFight(fight);                                               //И ЭТО БЛЯТЬ ПОБЕДА НАД БОССОМ!
                    fights.remove(fightId);
                    //TODO add rating to every fighter
                }
            }
        }
        return attack.toString();
    }

    @RequestMapping("/summon")
    public String summonAnimal(@RequestParam(name = "summonerNumber") int summonerNumber,
                               @RequestParam(name = "animalName") String name,
                               @RequestParam(name = "fightId") int id) {
        Fight fight = fights.get(id);
        if (fight == null) return "{\n\"code\": 2\n}";
        NinjaAnimal animal = ninjaAnimalService.get(name);
        Character summoner = (Character) fight.getFighters().get(summonerNumber).getValue();
        if (!summoner.getAnimalRace().equals(animal.getRace()) ||
                summoner.getUser().getStats().getLevel() < animal.getRequiredLevel())
            return "{\"code\": 4";                              //4 means user cannot summon this animal
        animal.prepareForFight();
        fight.addFighter(animal, fight.getFighters().get(summonerNumber).getKey());
        return "{ \"summoned\": true }";
    }

    private void stopFight(Fight fight) {
        if (fight instanceof FightPVP) pvpFightsService.addFight(((FightPVP) fight));
        else fightVsAIService.addFight(((FightVsAI) fight));
    }


}
