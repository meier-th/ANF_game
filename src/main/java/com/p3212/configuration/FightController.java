package com.p3212.configuration;

import com.p3212.EntityClasses.Attack;
import com.p3212.EntityClasses.Boss;
import com.p3212.EntityClasses.Fight;
import com.p3212.EntityClasses.Character;
import com.p3212.EntityClasses.Creature;
import com.p3212.EntityClasses.FightPVP;
import com.p3212.EntityClasses.FightVsAI;
import com.p3212.EntityClasses.Spell;
import com.p3212.Services.BossService;
import com.p3212.Services.FightVsAIService;
import com.p3212.Services.PVPFightsService;
import com.p3212.Services.SpellService;
import com.p3212.Services.UserService;
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

    private HashMap<String, Fight> fights;

    {
        fights = new HashMap<>();
    }

    @RequestMapping("/startPvp")
    public String startPvp(@RequestParam(name = "fighter1") String fighter1Name, @RequestParam(name = "fighter2") String fighter2Name) {
        FightPVP fight = new FightPVP();
        Character fighter1 = userService.getUser(fighter1Name).getCharacter();
        Character fighter2 = userService.getUser(fighter2Name).getCharacter(); //TODO
        fighter1.prepareForFight();
        fighter2.prepareForFight();
        fight.addFighter(fighter1, 1);
        fight.addFighter(fighter2, 2);
        fights.put(String.valueOf(fight.getId()), fight);
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
        fights.put(String.valueOf(fight.getId()), fight);
        return fight.toString();
    }

    @RequestMapping("/attack")
    public String attack(@RequestParam(name = "attackerNumber") String attackerNumber,
                         @RequestParam(name = "enemyNumber") String enemyNumber,
                         @RequestParam(name = "fightId") String fightId,
                         @RequestParam(name = "spellId") String spellId) {
        Fight fight = fights.get(fightId);
        if (fight == null) return "{\n\"code\": 2\n}";              //code 2 means fight doesn't exist
        int attackerNum = Integer.parseInt(attackerNumber) - 1;
        int enemyNum = Integer.parseInt(enemyNumber) - 1;
        Creature attacker = fight.getFighters().get(attackerNum).getValue();
        Creature enemy = fight.getFighters().get(enemyNum).getValue();
        Spell spell = spellService.get(Integer.parseInt(spellId));
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
                                .get(enemyNum)
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
