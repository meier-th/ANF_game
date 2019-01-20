package com.p3212.Configurations;

import com.p3212.EntityClasses.*;
import com.p3212.EntityClasses.Character;
import com.p3212.Services.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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
    UserAIFightService userAiFightService;

    @Autowired
    NinjaAnimalService ninjaAnimalService;

    @Autowired
    SpellHandlingService spellHandlingService;

    @Autowired
    private StatsService statsServ;

    @Autowired
    private WebSocketsController notifServ;

    @Autowired
    FightDataBean fightDataBean;

    ScheduledExecutorService scheduler;

    private ConcurrentHashMap<Integer, ScheduledFuture> timers;

    private ConcurrentHashMap<Integer, Fight> fights;
    private ConcurrentSkipListSet<String> usersInFight;
    private ConcurrentHashMap<Integer, ArrayDeque<String>> queues;
    private AtomicInteger queueSequence;

    @PostConstruct
    private void init() {
        fights = fightDataBean.getFights();
        usersInFight = fightDataBean.getUsersInFight();
        queues = fightDataBean.getQueues();
        queueSequence = new AtomicInteger();
        scheduler = Executors.newScheduledThreadPool(1);
        timers = new ConcurrentHashMap<>();
    }

    @RequestMapping("/createQueue")
    public ResponseEntity<?> createQueue() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if (usersInFight.contains(name)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("{ \"code\": 7}");    // 7 - user is busy
        }
        final int id = queueSequence.getAndIncrement();
        queues.put(id, new ArrayDeque<>());
        queues.get(id).add(name);
        return ResponseEntity.status(HttpStatus.OK).body("{\"queueId\":" + id + "}");
    }

    @RequestMapping("/closeQueue")
    public void closeQueue(@RequestParam int id) {
        queues.remove(id);
    }

    @RequestMapping("/invite")
    public void invite(@RequestParam String username, @RequestParam String type, @RequestParam int id) {
        notifServ.sendInvitation(username,
                SecurityContextHolder.getContext().getAuthentication().getName(), type, id);
    }

    @RequestMapping("/join")
    public ResponseEntity join(@RequestParam String author, @RequestParam int id) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if (usersInFight.contains(name)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("{ \"code\": 7}");    // 7 - user is busy
        }
        queues.get(id).add(name);
        notifServ.sendApproval(author, name, id);
        return ResponseEntity.status(HttpStatus.OK).body("{\"answer\": \"OK\"}");
    }

    @PostMapping("info")
    public ResponseEntity info(@RequestParam int id) {
        Fight fight = fights.get(id);
        fight.setTimeLeft(timers.get(id).getDelay(TimeUnit.MILLISECONDS));
        return ResponseEntity.status(HttpStatus.OK).body(fight.toString());
    }

    @RequestMapping("/startPvp")
    public ResponseEntity<?> startPvp(@RequestParam(name = "queueId") int queueId) {
        if (!queues.containsKey(queueId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"code\": 2,\"error\":\"Queue doesn't exist\"}");
        }
        if (queues.get(queueId).size() != 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"code\": 8,\"error\":\"The number of players should be equal to 2\"}");
        }
        FightPVP fight = new FightPVP();
        String fighter2Name = queues.get(queueId).pop();
        String fighter1Name = queues.get(queueId).pop();
        Character fighter1 = userService.getUser(fighter1Name).getCharacter();
        Character fighter2 = userService.getUser(fighter2Name).getCharacter();
        if (fighter1 == null || fighter2 == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{ \"code\": 3}"); //code 3 means fighter does't exist
        }
        usersInFight.add(fighter1Name);
        usersInFight.add(fighter2Name);
        fighter1.prepareForFight();
        fighter2.prepareForFight();
        fight.setFighters(fighter1, fighter2);
        int biggerRating = 15 + Math.abs(fighter1.getUser().getStats().getRating() - fighter2.getUser().getStats().getRating()) / 4;
        int lesserRating = 15 - Math.abs(fighter1.getUser().getStats().getRating() - fighter2.getUser().getStats().getRating()) / 8;
        if (lesserRating < 5) {
            lesserRating = 5;
        }
        fight.setBiggerRatingChange(biggerRating);
        fight.setLessRatingChange(lesserRating);
        fights.put(fight.getId(), fight);
        final String name = SecurityContextHolder.getContext().getAuthentication().getName();
        String user = name.equals(fighter1Name) ? fighter2Name : fighter1Name;
        notifServ.sendStart(name, user, fight.getId());
        queues.remove(queueId);
        timers.put(fight.getId(), scheduler.schedule(() -> schedule(fight),3010, TimeUnit.MILLISECONDS));
        return ResponseEntity.status(HttpStatus.OK).body(fight.toString());
    }

    @RequestMapping("/startPve")
    public ResponseEntity<?> startPve(@RequestParam(name = "queueId") int queueId, @RequestParam(name = "bossId") int bossId) {
        if (!queues.containsKey(queueId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Queue doesn't exist\"}");
        }
        String[] fighters = (String[]) queues.get(queueId).toArray();
        for (String fighter : fighters) {
            if (usersInFight.contains(fighter)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("{ \"code\": 7}");
            }
        }
        if (usersInFight.contains(String.valueOf(bossId))) {
            ResponseEntity.status(HttpStatus.CONFLICT).body("{ \"code\": 7}");
        }
        FightVsAI fight = new FightVsAI();
        for (String fighterName : fighters) {
            Character fighter = userService.getUser(fighterName).getCharacter();
            fighter.prepareForFight();
            fight.addFighter(fighter);
        }
        Boss boss = bossService.getBoss(bossId);
        boss.prepareForFight();
        fight.setBoss(boss);
        fight.setBoss(boss);
        Collections.addAll(usersInFight, fighters);
        usersInFight.add(String.valueOf(bossId));
        fights.put(fight.getId(), fight);
        final String name = SecurityContextHolder.getContext().getAuthentication().getName();
        queues.get(queueId).forEach((user) -> {
            if (!user.equals(name)) {
                notifServ.sendStart(name, user, fight.getId());
            }
        });
        queues.remove(queueId);
        return ResponseEntity.status(HttpStatus.OK).body(fight.toString());
    }

    @RequestMapping("/attack")
    public ResponseEntity<?> attackHandler(@RequestParam String enemy,
                                           @RequestParam int fightId,
                                           @RequestParam String spellName) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Fight fight = fights.get(fightId);
        if (fight == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\n\"code\": 2\n}");              // code 2 means fight doesn't exist
        }
        if (!name.equals(fight.getCurrentAttacker(0))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"code\": 10}");                 // 10 - not your turn
        }

        Attack attack;
        timers.get(fightId).cancel(true);
        if (fight instanceof FightPVP) {
            attack = attackPvp(name, enemy, fightId, spellName);
        } else {
            attack = attackPve(name, fightId, spellName);
        }
        if (attack.getCode() != 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(attack.toString());
        }
        fight.switchAttacker();
        timers.get(fightId).cancel(true);
        timers.put(fightId, scheduler.schedule(() -> schedule(fight), 30, TimeUnit.SECONDS));
        if (fight.getCurrentAttacker(0).length() < 6) {
            // TODO perform NPC attack
        }

        return ResponseEntity.status(HttpStatus.OK).body(attack.toString());
    }

    private void schedule(Fight fight) {
        fight.switchAttacker();
        if (fight instanceof FightPVP) {
            notifServ.sendSwitch(((FightPVP) fight).getFighter1().getLogin(), fight.getCurrentAttacker(0));
            notifServ.sendSwitch(((FightPVP) fight).getFighter2().getLogin(), fight.getCurrentAttacker(0));
        } else {
            ((FightVsAI) fight).getSetFighters().forEach((user) ->
                    notifServ.sendSwitch(user.getFighter().getUser().getLogin(), fight.getCurrentAttacker(0)));
        }
        timers.put(fight.getId(), scheduler.schedule(() -> schedule(fight), 30, TimeUnit.SECONDS));
    }

    private Attack attackPvp(String attackerName, String enemyName, int fightId, String spellName) {
        Attack attack = new Attack();
        FightPVP fight = (FightPVP) fights.get(fightId);
        User attacker = fight.getFighter1();
        User enemy = fight.getFighter2();
        if (attacker == null || enemy == null) {
            attack.setCode(6);
            return attack;
        }
        if (enemy.getLogin().equals(attackerName)) {
            User tmp = attacker;
            attacker = enemy;
            enemy = tmp;
        }
        int damage;
        int chakra;
        if (!spellName.equalsIgnoreCase("Physical attack")) {
            Spell spell = spellService.get(spellName);
            SpellHandling handling = spellHandlingService.getSpellHandling(attacker.getCharacter(), spell);
            if (spell == null) {
                attack.setCode(8);
                return attack;
            }
            if (spellName.equals("Air Strike")) {
                damage = spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel();
            } else {
                damage = Math.round((spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel()) * (1 - enemy.getCharacter().getResistance()));
            }
            if (spellName.equals("Fire Strike") && enemy.getCharacter().getResistance() < 0.8) {
                damage *= 2;
            }
            chakra = spell.getBaseChakraConsumption()
                    + handling.getSpellLevel() * spell.getChakraConsumptionPerLevel();
        } else {
            damage = Math.round(attacker.getCharacter().getPhysicalDamage() * (1 - enemy.getCharacter().getResistance()));
            chakra = 0;
        }
        int chakraBurn = 0;
        if (spellName.equals("Water Strike")) {
            chakraBurn = damage / 10;
        }
        attack.setDamage(damage);
        attack.setChakra(chakra);
        attacker.getCharacter().spendChakra(chakra);
        enemy.getCharacter().acceptDamage(damage);
        attack.setDeadly(enemy.getCharacter().getCurrentHP() <= 0);

        sendAfterAttack(enemyName, damage, enemyName,
                attackerName, fight.getNextAttacker(), attack.isDeadly(),
                attack.isDeadly(), spellName, chakra, chakraBurn);
        sendAfterAttack(attackerName, damage, enemyName,
                attackerName, fight.getNextAttacker(), attack.isDeadly(),
                attack.isDeadly(), spellName, chakra, chakraBurn);
        if (attack.isDeadly()) {
            if (fight.getFighter1().getLogin().equals(enemyName)) {
                fight.setFirstWon(false);
            } else {
                fight.setFirstWon(true);
            }
            int firstFighterPreviousRating = fight.getFighter1().getStats().getRating();
            int secondFighterPreviousRating = fight.getFighter2().getStats().getRating();
            int rating;
            if (firstFighterPreviousRating >= secondFighterPreviousRating && fight.isFirstWon() || secondFighterPreviousRating >= firstFighterPreviousRating && !fight.isFirstWon()) {
                rating = fight.getLessRatingChange();
            } else {
                rating = fight.getBiggerRatingChange();
            }
            fight.setRatingChange(rating);
            if (fight.isFirstWon()) {
                fight.getFighter1().getStats().setRating(fight.getFighter1().getStats().getRating() + rating);
                fight.getFighter1().getStats().setFights(fight.getFighter1().getStats().getFights() + 1);
                fight.getFighter1().getStats().setWins(fight.getFighter1().getStats().getWins() + 1);
                fight.getFighter2().getStats().setRating(fight.getFighter2().getStats().getRating() - rating);
                fight.getFighter2().getStats().setFights(fight.getFighter2().getStats().getFights() + 1);
                fight.getFighter2().getStats().setLosses(fight.getFighter2().getStats().getLosses() + 1);
            } else {
                fight.getFighter1().getStats().setRating(fight.getFighter1().getStats().getRating() - rating);
                fight.getFighter1().getStats().setFights(fight.getFighter1().getStats().getFights() + 1);
                fight.getFighter1().getStats().setLosses(fight.getFighter1().getStats().getLosses() + 1);
                fight.getFighter2().getStats().setRating(fight.getFighter2().getStats().getRating() + rating);
                fight.getFighter2().getStats().setFights(fight.getFighter2().getStats().getFights() + 1);
                fight.getFighter2().getStats().setWins(fight.getFighter2().getStats().getWins() + 1);
            }
            statsServ.addStats(fight.getFighter1().getStats());
            statsServ.addStats(fight.getFighter2().getStats());
            fight.setFirstFighter(fight.getFighter1().getCharacter());
            fight.setSecondFighter(fight.getFighter2().getCharacter());
            pvpFightsService.addFight(fight);
            timers.remove(fightId).cancel(true);
            fights.remove(fightId);
            usersInFight.remove(attackerName);
            usersInFight.remove(enemyName);
        }
        return attack;
    }

    private Attack attackPve(String attackerName, int fightId, String spellName) {
        Attack attack = new Attack();
        FightVsAI fight = (FightVsAI) fights.get(fightId);
        Boss boss = fight.getBoss();
        User attacker = userService.getUser(attackerName);
        int damage;
        int chakra;
        if (!spellName.equalsIgnoreCase("Physical attack")) {
            Spell spell = spellService.get(spellName);
            SpellHandling handling = spellHandlingService.getSpellHandling(attacker.getCharacter(), spell);
            if (spell == null) {
                attack.setCode(8);
                return attack;
            }
            if (spellName.equals("Air Strike")) {
                damage = spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel();
            } else {
                damage = Math.round((spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel()) * (1 - boss.getResistance()));
            }
            if (spellName.equals("Fire Strike") && boss.getResistance() < 0.8) {
                damage *= 2;
            }
            chakra = spell.getBaseChakraConsumption()
                    + handling.getSpellLevel() * spell.getChakraConsumptionPerLevel();
        } else {
            damage = Math.round(attacker.getCharacter().getPhysicalDamage() * (1 - boss.getResistance()));
            chakra = 0;
        }
        attack.setDamage(damage);
        attack.setChakra(chakra);
        attacker.getCharacter().spendChakra(chakra);
        boss.acceptDamage(damage);
        attack.setDeadly(boss.getCurrentHP() <= 0);
        List<User> fighters = fight.getSetFighters().stream().map(uinF -> uinF.getFighter().getUser()).collect(Collectors.toList());
        for (User fighter : fighters) {
            sendAfterAttack(fighter.getLogin(), damage, boss.getName(), attacker.getLogin(), fight.getNextAttacker(), attack.isDeadly(), attack.isDeadly(), spellName, chakra, 0);
        }
        if (attack.isDeadly()) {
            fightVsAIService.addFight(fight);
            for (UserAIFight fightData : fight.getSetFighters()) {
                if (!fightData.getResult().equals(UserAIFight.Result.DIED))
                    fightData.setResult(UserAIFight.Result.WON);
                int experience = 500 + 200 * boss.getNumberOfTails();
                if (fightData.getResult().equals(UserAIFight.Result.DIED)) {
                    experience /= 2;
                    fightData.getFighter().getUser().getStats().setFights(fightData.getFighter().getUser().getStats().getFights() + 1);
                    fightData.getFighter().getUser().getStats().setDeaths(fightData.getFighter().getUser().getStats().getDeaths() + 1);
                    fightData.getFighter().changeXP(experience);
                    statsServ.addStats(fightData.getFighter().getUser().getStats());
                } else {
                    fightData.getFighter().getUser().getStats().setFights(fightData.getFighter().getUser().getStats().getFights() + 1);
                    fightData.getFighter().getUser().getStats().setWins(fightData.getFighter().getUser().getStats().getWins() + 1);
                    fightData.getFighter().changeXP(experience);
                    statsServ.addStats(fightData.getFighter().getUser().getStats());
                }
                fightData.setExperience(experience);
                userAiFightService.add(fightData);
            }
            queues.remove(fightId);
            for (User fighter : fighters) {
                usersInFight.remove(fighter.getLogin());
            }
        } else {
            if (fight.getNextAttacker().equals(boss.getName())) {
                //boss attack
            } else if (isAnimalName(fight.getNextAttacker())) {
                //animal attack
            }
        }
        return attack;
    }

    private boolean isAnimalName(String name) {
        int index = name.lastIndexOf(':'); // user:animal
        return (index != -1);
    }

    private void compareStats(ArrayList<User> before, ArrayList<User> after) {
        String report = "";
        for (int i = 0; i < 10; ++i) {
            if (!(before.get(i).equals(after.get(i)))) {
                report += "User " + after.get(i).getLogin() + " is now on the " + i + " place.\n";
            }
        }
        if (report.equals("")) {
            return;
        }
        /*Message warning = new Message();
        warning.setAuthor("SYSTEM");
        warning.setText("Users in top-10 have changed their positions:\n" + report);
        notifServ.notify(warning);*/
        String warning = "SYSTEM:Users in top-10 have changed their positions:\n" + report;
        notifServ.notify(warning);
    }

    /**
     * @param username   Receiver of a message on websocket
     * @param damage
     * @param targetName Username of the target
     * @param attacker   Username of the attacker
     * @param next       Username of the next cha
     * @param dead
     * @param allDead
     * @param attackName
     * @param chakraCost
     * @param chakraBurn
     */
    private void sendAfterAttack(String username, int damage, String targetName, String attacker, String next, boolean dead, boolean allDead, String attackName, int chakraCost, int chakraBurn) {
        State state = new State(attacker, targetName, attackName, chakraCost, damage, chakraBurn, dead, allDead, next);
        notifServ.sendFightState(state, username);
    }

}
