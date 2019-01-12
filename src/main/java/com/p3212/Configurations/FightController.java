package com.p3212.Configurations;

import com.p3212.EntityClasses.*;
import com.p3212.EntityClasses.Character;
import com.p3212.Repositories.StatsRepository;
import com.p3212.Services.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.annotation.PostConstruct;

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

    @Autowired
    SpellHandlingService spellHandlingService;

    @Autowired
    private StatsRepository statsRep;

    @Autowired
    private WebSocketsController notifServ;

    @Autowired
    FightDataBean fightDataBean;

    private ConcurrentHashMap<Integer, Fight> fights;
    private ConcurrentSkipListSet<String> usersInFight;
    private ConcurrentHashMap<Integer, ArrayDeque<String>> queues;

    @PostConstruct
    private void init() {
        fights = fightDataBean.getFights();
        usersInFight = fightDataBean.getUsersInFight();
        queues = fightDataBean.getQueues();
    }

    @RequestMapping("/acceptQueue")
    public ResponseEntity<?> acceptQueue(@RequestParam(name = "queueId") int id) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if (usersInFight.contains(name))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("{ \"code\": 7}");    // 7 - user is busy
        if (!queues.containsKey(id)) queues.put(id, new ArrayDeque<>());
        queues.get(id).push(name);
        return ResponseEntity.status(HttpStatus.OK).body("{\"answer\":\"Succeeded\"}");
    }

    @RequestMapping("/startPvp")
    public ResponseEntity<?> startPvp(@RequestParam(name = "queueId") int queueId) {
        if (!queues.containsKey(queueId))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Queue doesn't exist\"}");
        if (queues.get(queueId).size() != 2)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"The number of players should be equal to 2\"}");
        FightPVP fight = new FightPVP();
        String fighter2Name = queues.get(queueId).pop();
        String fighter1Name = queues.get(queueId).pop();
        Character fighter1 = userService.getUser(fighter1Name).getCharacter();
        Character fighter2 = userService.getUser(fighter2Name).getCharacter();
        if (fighter1 == null || fighter2 == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{ \"code\": 3}"); //code 3 means fighter does't exist
        usersInFight.add(fighter1Name);
        usersInFight.add(fighter2Name);
        fighter1.prepareForFight();
        fighter2.prepareForFight();
        fight.addFighter(fighter1, 1);
        fight.addFighter(fighter2, 2);
        int biggerRating = 15 + Math.abs(fighter1.getUser().getStats().getRating() - fighter2.getUser().getStats().getRating()) / 4;
        int lesserRating = 15 - Math.abs(fighter1.getUser().getStats().getRating() - fighter2.getUser().getStats().getRating()) / 8;
        if (lesserRating < 5)
            lesserRating = 5;
        fight.setBiggerRatingChange(biggerRating);
        fight.setLessRatingChange(lesserRating);
        fights.put(fight.getId(), fight);
        queues.remove(queueId);
        return ResponseEntity.status(HttpStatus.OK).body(fight.toString());
    }

    @RequestMapping("/startPve")
    public ResponseEntity<?> startPve(@RequestParam(name = "queueId") int queueId, @RequestParam(name = "bossId") int bossId) {
        if (!queues.containsKey(queueId))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Queue doesn't exist\"}");
        String[] fighters = (String[]) queues.get(queueId).toArray();
        for (String fighter : fighters) {
            if (usersInFight.contains(fighter))
                return ResponseEntity.status(HttpStatus.CONFLICT).body("{ \"code\": 7}");
        }
        if (usersInFight.contains(String.valueOf(bossId)))
            ResponseEntity.status(HttpStatus.CONFLICT).body("{ \"code\": 7}");
        FightVsAI fight = new FightVsAI();
        for (String fighterName : fighters) {
            Character fighter = userService.getUser(fighterName).getCharacter();
            fighter.prepareForFight();
            fight.addFighter(fighter, 1);
        }
        Boss boss = bossService.getBoss(bossId);
        boss.prepareForFight();
        fight.addFighter(boss, 2);
        fight.setBoss(boss);
        Collections.addAll(usersInFight, fighters);
        usersInFight.add(String.valueOf(bossId));
        fights.put(fight.getId(), fight);
        queues.remove(queueId);
        return ResponseEntity.status(HttpStatus.OK).body(fight.toString());
    }

    @RequestMapping("/attack")
    public ResponseEntity<?> attackHandler(@RequestParam(name = "enemyNumber") int enemyNumber,
                                           @RequestParam(name = "fightId") int fightId,
                                           @RequestParam(name = "spellId") int spellId) {
        Fight fight = fights.get(fightId);
        if (fight == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\n\"code\": 2\n}");              //code 2 means fight doesn't exist
        Attack attack = attack(fight.getCurrentAttacker() + 1, enemyNumber, fightId, spellId);

        if (attack.getCode() != 0) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(attack.toString());
        fight.switchAttacker();
        return ResponseEntity.status(HttpStatus.OK).body(attack.toString());
    }

    private Attack attack(int attackerNumber, int enemyNumber, int fightId, int spellId) {
        Fight fight = fights.get(fightId);
        Creature attacker;
        Creature enemy;
        Attack attack = new Attack();
        if (fight.getFighters().get(--attackerNumber).getKey().equals(fight.getFighters().get(--enemyNumber).getKey())) {
            attack.setCode(6); //6 means attack of a teammate
            return attack;
        }
        try {
            attacker = fight.getFighters().get(attackerNumber).getValue();
            enemy = fight.getFighters().get(enemyNumber).getValue();
        } catch (Exception ex) {
            attack.setCode(5);               //code 5 means there's no fighters with that number
            return attack;
        }
        Spell spell;
        if (attacker instanceof Character) {
            spell = spellService.get(spellId);
            if (spell == null || spellHandlingService.getSpellHandling(((Character) attacker), spell) == null) {
                attack.setCode(8); //8 means user can't use this spell
                return attack;
            }
        } else {
            int damage = attacker instanceof Boss ? ((Boss) attacker).getNumberOfTails() * 30 : ((NinjaAnimal) attacker).getDamage();
            spell = new Spell("npc attack", "furious scratching", damage, 0);
            spell.setBaseChakraConsumption(15);
        }
        int spellLvl = attacker instanceof Character
                ? spellHandlingService.getSpellHandling((Character) attacker, spell).getSpellLevel() : 1;
        attack = spell.performAttack(spellLvl, enemy.getResistance());
        if (attacker.getCurrentChakra() < attack.getChakra()) {
            if (attacker instanceof NinjaAnimal)
                fight.getFighters().remove(attackerNumber);         //animals disappear when no chakra
            attack.setCode(1);
            return attack;
        }                         //code 1 means attacker doesn't have enough chakra
        enemy.acceptDamage(attack.getDamage());
        if (enemy.getCurrentHP() <= 0) {
            attack.setDeadly(true);
            if (enemy instanceof NinjaAnimal) {
                fight.getFighters().remove(enemyNumber);
                return attack;
            }
            if (fight instanceof FightPVP) {
                ((FightPVP) fight).setFighters((Character) attacker, (Character) enemy);
                ((FightPVP) fight).setFirstWon(
                        fights.get(fightId)
                                .getFighters()
                                .get(enemyNumber)
                                .getKey() == 1);
                stopFight(fightId);
            } else {
                if (fight.getFighters().get(enemyNumber).getKey() == 1) {
                    ((Character) enemy).changeXP(((FightVsAI) fight).getBoss().getNumberOfTails() * 10);
                    if (fight.getFighters().size() < 2) {
                        stopFight(fightId);
                    }
                    fight.getFighters().remove(enemyNumber);
                    usersInFight.remove(((Character) enemy).getUser().getLogin());
                    return attack;
                } else {
                    fight.getFighters().remove(enemyNumber);
                    fight.getFighters().iterator().forEachRemaining(item -> {
                        if (item.getValue() instanceof Character) {
                            ((Character) item.getValue()).changeXP(((FightVsAI) fight).getBoss().getNumberOfTails() * 100);
                        }
                    });
                    stopFight(fightId);
                }
            }
        }
        return attack;
    }

    @RequestMapping("/summon")
    public ResponseEntity<?> summonAnimal(@RequestParam(name = "summonerNumber") int summonerNumber,
                                          @RequestParam(name = "animalName") String name,
                                          @RequestParam(name = "fightId") int id) {
        Fight fight = fights.get(id);
        if (fight == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{ \"code\": 2}");
        NinjaAnimal animal = ninjaAnimalService.get(name);
        Character summoner = (Character) fight.getFighters().get(summonerNumber).getValue();
        if (!summoner.getAnimalRace().equals(animal.getRace()) ||
                summoner.getUser().getStats().getLevel() < animal.getRequiredLevel())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{ \"code\": 4}"); //4 means user cannot summon this animal
        animal.prepareForFight();
        fight.addFighter(animal, fight.getFighters().get(summonerNumber).getKey());
        return ResponseEntity.status(HttpStatus.OK).body("{ \"summoned\": true }");
    }

    private void stopFight(int fightId) {
        ArrayList<User> usersBefore = new ArrayList<>();
        Page<Stats> stts = statsRep.getTopStats(PageRequest.of(0, 10));
        for (Stats st : stts) {
            usersBefore.add(st.getUser());
        }
        Fight fight = fights.get(fightId);
        if (fight instanceof FightPVP) {
            int lvlDiff = ((FightPVP) fight).getFirstFighter().getLevel() -
                    ((FightPVP) fight).getSecondFighter().getLevel();
            boolean isFirstWon = ((FightPVP) fight).isFirstWon();
            int firstFighterPreviousRating = ((FightPVP) fight).getFirstFighter().getUser().getStats().getRating();
            int secondFighterPreviousRating = ((FightPVP) fight).getSecondFighter().getUser().getStats().getRating();
            int rating;
            if (firstFighterPreviousRating >= secondFighterPreviousRating && isFirstWon || secondFighterPreviousRating >= firstFighterPreviousRating && !isFirstWon)
                rating = ((FightPVP) fight).getLessRatingChange();
            else
                rating = ((FightPVP) fight).getBiggerRatingChange();
            ((FightPVP) fight).setRatingChange(rating);
            ((FightPVP) fight).getFirstFighter().changeRating(isFirstWon ? rating : -rating);
            ((FightPVP) fight).getSecondFighter().changeRating(isFirstWon ? -rating : rating);
            pvpFightsService.addFight(((FightPVP) fight));
        } else fightVsAIService.addFight(((FightVsAI) fight));
        fights.remove(fightId);
        fight.getFighters().iterator().forEachRemaining(fighter -> {
            if (fighter.getValue() instanceof Boss)
                usersInFight.remove(String.valueOf(((Boss) fighter.getValue()).getId()));
            else usersInFight.remove(((Character) fighter.getValue()).getUser().getLogin());
        });
        ArrayList<User> usersAfter = new ArrayList<>();
        Page<Stats> stats = statsRep.getTopStats(PageRequest.of(0, 10));
        for (Stats st : stats) {
            usersAfter.add(st.getUser());
        }
        compareStats(usersBefore, usersAfter);
    }

    private void compareStats(ArrayList<User> before, ArrayList<User> after) {
        String report = "";
        for (int i = 0; i < 10; ++i) {
            if (!(before.get(i).equals(after.get(i))))
                report += "User " + after.get(i).getLogin() + " is now on the " + i + " place.\n";
        }
        if (report.equals(""))
            return;
        /*Message warning = new Message();
        warning.setAuthor("SYSTEM");
        warning.setText("Users in top-10 have changed their positions:\n" + report);
        notifServ.notify(warning);*/
        String warning = "SYSTEM:Users in top-10 have changed their positions:\n"+report;
        notifServ.notify(warning);
    }


}
