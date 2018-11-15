package com.p3212.configuration;

import com.p3212.EntityClasses.*;
import com.p3212.EntityClasses.Character;
import com.p3212.Repositories.SpellRepository;
import com.p3212.Repositories.UserRepository;
import com.p3212.Services.FightVsAIService;
import com.p3212.Services.PVPFightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/fight")
public class FightController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    SpellRepository spellRepository;

    @Autowired
    PVPFightsService pvpFightsService;

    @Autowired
    FightVsAIService fightVsAIService;

    private HashMap<String, Fight> fights;

    {
        fights = new HashMap<>();
    }

    @RequestMapping("/startPvp")
    public String startPvp(@RequestParam(name = "fighter1") String fighter1Name, @RequestParam(name = "fighter2") String fighter2Name) {
        FightPVP fight = new FightPVP();
        Character fighter1 = userRepository.findById(fighter1Name).get().getCharacter(); //TODO
        Character fighter2 = userRepository.findById(fighter2Name).get().getCharacter(); //TODO
        fight.addFighter(fighter1, 1);
        fight.addFighter(fighter2, 2);
        fights.put(String.valueOf(fight.getId()), fight);
        return String.valueOf(fight.getId());
    }

    @RequestMapping("/attack")
    public String attack(@RequestParam(name = "attackerNumber") String attackerNumber,
                         @RequestParam(name = "enemyNumber") String enemyNumber,
                         @RequestParam(name = "fightId") String fightId,
                         @RequestParam(name = "spellId") String spellId) {
        Fight fight = fights.get(fightId);
        if (fight == null) return "{\n\"code\": 2\n}";              //code 2 means fight doesn't exist
        Creature attacker = fight.getFighters().get(Integer.parseInt(attackerNumber)).getValue();
        Creature enemy = fight.getFighters().get(Integer.parseInt(enemyNumber)).getValue();
        Spell spell = spellRepository.findById(Integer.parseInt(spellId)).get(); //TODO
        Attack attack = spell.performAttack(attacker.getLevel(), enemy.getResistance());
        if (attacker.getCurrentChakra() < attack.getChakra())
            return "{\n\"code\": 1\n}";                             //code 1 means attacker doesn't have enough chakra
        enemy.acceptDamage(attack.getDamage());
        if (enemy.getCurrentHP() <= 0) {
            attack.setDeadly(true);
            if (fight instanceof FightPVP) {
                ((FightPVP) fight).setFighters((Character) attacker, (Character) enemy);
                ((FightPVP) fight).setFirstWon(
                        fights.get(fightId)
                                .getFighters()
                                .get(Integer.parseInt(enemyNumber))
                                .getKey() == 1);
                stopFight(fight);
                fights.remove(fightId);
                //TODO rating change not defined
            } else {
                //TODO actions for boss fight
            }
        }
        return attack.toString();
    }

    private void stopFight(Fight fight) {
        if (fight instanceof FightPVP) pvpFightsService.addFight(((FightPVP) fight));
        else fightVsAIService.addFight(((FightVsAI) fight));
    }


}
