package com.anf.config;

import com.anf.model.Attack;
import com.anf.model.Fight;
import com.anf.model.NinjaAnimal;
import com.anf.model.NinjaAnimalRace;
import com.anf.model.State;
import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.Boss;
import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Spell;
import com.anf.model.database.SpellKnowledge;
import com.anf.model.database.User;
import com.anf.service.BossService;
import com.anf.service.FightVsAIService;
import com.anf.service.NinjaAnimalService;
import com.anf.service.PVPFightsService;
import com.anf.service.SpellKnowledgeService;
import com.anf.service.SpellService;
import com.anf.service.StatsService;
import com.anf.service.UserAIFightService;
import com.anf.service.UserService;
import com.anf.service.state.FightStateStore;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fight")
@RequiredArgsConstructor
public class FightController {

  private final UserService userService;
  private final SpellService spellService;
  private final BossService bossService;
  private final PVPFightsService pvpFightsService;
  private final FightVsAIService fightVsAIService;
  private final UserAIFightService userAiFightService;
  private final NinjaAnimalService ninjaAnimalService;
  private final SpellKnowledgeService SpellKnowledgeService;
  private final StatsService statsServ;
  private final WebSocketsController notifServ;
  private final FightStateStore fightStateStore;

  private ScheduledExecutorService scheduler;
  private ConcurrentHashMap<Integer, ScheduledFuture> timers;

  @PostConstruct
  private void init() {
    scheduler = Executors.newScheduledThreadPool(1);
    timers = new ConcurrentHashMap<>();
  }

  @RequestMapping("/createQueue")
  public ResponseEntity<?> createQueue() {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    if (fightStateStore.isUserInFight(name)) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("{ \"code\": 7}"); // 7 - user is busy
    }
    int id = fightStateStore.nextQueueId();
    fightStateStore.createQueue(id, name);
    return ResponseEntity.status(HttpStatus.OK).body("{\"queueId\":" + id + "}");
  }

  @RequestMapping("/closeQueue")
  public void closeQueue(@RequestParam int id) {
    fightStateStore.removeQueue(id);
  }

  @RequestMapping("/invite")
  public void invite(
      @RequestParam String username, @RequestParam String type, @RequestParam int id) {
    notifServ.sendInvitation(
        username, SecurityContextHolder.getContext().getAuthentication().getName(), type, id);
  }

  @RequestMapping("/join")
  public ResponseEntity join(@RequestParam String author, @RequestParam int id) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    if (fightStateStore.isUserInFight(name)) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("{ \"code\": 7}"); // 7 - user is busy
    }
    if (!fightStateStore.queueExists(id)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\"code\": 2,\"error\":\"Queue doesn't exist\"}");
    }
    fightStateStore.addUserToQueue(id, name);
    notifServ.sendApproval(author, name, id);
    return ResponseEntity.status(HttpStatus.OK).body("{\"answer\": \"OK\"}");
  }

  @PostMapping("info")
  public ResponseEntity info(@RequestParam int id) {
    Fight fight = fightStateStore.getFight(id).orElse(null);
    if (fight == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\n\"code\": 2\n}"); // code 2 means fight doesn't exist
    }
    if (timers.get(id) != null) {
      fight.setTimeLeft(timers.get(id).getDelay(TimeUnit.MILLISECONDS));
    }
    fightStateStore.saveFight(fight);
    return ResponseEntity.status(HttpStatus.OK).body(fight.toString());
  }

  @RequestMapping("/startPvp")
  public ResponseEntity<?> startPvp(@RequestParam(name = "queueId") int queueId) {
    if (!fightStateStore.queueExists(queueId)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\"code\": 2,\"error\":\"Queue doesn't exist\"}");
    }
    if (fightStateStore.queueSize(queueId) != 2) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("{\"code\": 8,\"error\":\"The number of players should be equal to 2\"}");
    }
    FightPVP fight = new FightPVP();
    String fighter2Name = fightStateStore.popQueueUser(queueId);
    String fighter1Name = fightStateStore.popQueueUser(queueId);
    GameCharacter fighter1 = userService.getUser(fighter1Name).getCharacter();
    GameCharacter fighter2 = userService.getUser(fighter2Name).getCharacter();
    if (fighter1 == null || fighter2 == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{ \"code\": 3}"); // code 3 means fighter does't exist
    }
    fightStateStore.markUserInFight(fighter1Name);
    fightStateStore.markUserInFight(fighter2Name);
    fighter1.prepareForFight();
    fighter2.prepareForFight();
    fight.setFighters(fighter1, fighter2);
    int biggerRating =
        15
            + Math.abs(
                    fighter1.getUser().getStats().getRating()
                        - fighter2.getUser().getStats().getRating())
                / 4;
    int lesserRating =
        15
            - Math.abs(
                    fighter1.getUser().getStats().getRating()
                        - fighter2.getUser().getStats().getRating())
                / 8;
    if (lesserRating < 5) {
      lesserRating = 5;
    }
    fight.setBiggerRatingChange(biggerRating);
    fight.setLessRatingChange(lesserRating);
    fightStateStore.saveFight(fight);
    final String name = SecurityContextHolder.getContext().getAuthentication().getName();
    String user = name.equals(fighter1Name) ? fighter2Name : fighter1Name;
    notifServ.sendStart(name, user, fight.getId());
    fightStateStore.removeQueue(queueId);
    timers.put(
        fight.getId(),
        scheduler.schedule(() -> schedule(fight, true), 3010, TimeUnit.MILLISECONDS));
    return ResponseEntity.status(HttpStatus.OK).body(fight.toString());
  }

  @RequestMapping("/startPve")
  public ResponseEntity<?> startPve(
      @RequestParam(name = "queueId") int queueId, @RequestParam(name = "bossId") String bossName) {
    if (!fightStateStore.queueExists(queueId)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\"error\":\"Queue doesn't exist\"}");
    }
    var fighters = new ArrayList<>(fightStateStore.queueUsers(queueId));
    if (fighters.stream().anyMatch(fightStateStore::isUserInFight))
      return ResponseEntity.status(HttpStatus.CONFLICT).body("{ \"code\": 7}");
    if (fightStateStore.isUserInFight(bossName)) {
      ResponseEntity.status(HttpStatus.CONFLICT).body("{ \"code\": 7}");
    }
    FightVsAI fight = new FightVsAI();
    ArrayList<AiFightParticipation> userFights = new ArrayList<>();
    System.out.println("PVE fight began. Fighters:");
    for (String fighterName : fighters) {
      AiFightParticipation userF = new AiFightParticipation();
      userF.setFight(fight);
      GameCharacter fighter = userService.getUser(fighterName).getCharacter();
      userF.setFighter(fighter);
      fighter.prepareForFight();
      fight.addFighter(fighter);
      userFights.add(userF);
      System.out.println(fighterName);
    }
    fight.setSetFighters(userFights);
    Boss boss = bossService.getBossByName(bossName);
    boss.prepareForFight();
    fight.setBoss(boss);
    fightStateStore.markUsersInFight(fighters);
    fightStateStore.saveFight(fight);
    final String name = SecurityContextHolder.getContext().getAuthentication().getName();
    fighters.forEach(
        (user) -> {
          if (!user.equals(name)) {
            notifServ.sendStart(name, user, fight.getId());
          }
        });
    fightStateStore.removeQueue(queueId);
    timers.put(
        fight.getId(),
        scheduler.schedule(() -> schedule(fight, true), 3010, TimeUnit.MILLISECONDS));
    return ResponseEntity.status(HttpStatus.OK).body(fight.toString());
  }

  @RequestMapping("/attack")
  public ResponseEntity<?> attackHandler(
      @RequestParam String enemy, @RequestParam int fightId, @RequestParam String spellName) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    Fight fight = fightStateStore.getFight(fightId).orElse(null);
    if (fight == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\n\"code\": 2\n}"); // code 2 means fight doesn't exist
    }
    if (!name.equals(fight.getCurrentAttacker(0))) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("{\"code\": 10}"); // 10 - not your turn
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

    schedule(fight, false);

    return ResponseEntity.status(HttpStatus.OK).body(attack.toString());
  }

  private void schedule(Fight fight, boolean first) {
    if (first) {
      fight.switchAttacker();
    } else {
      if (fight instanceof FightVsAI) {
        String current = fight.getCurrentAttacker(0);
        fight.switchAttacker();
        if (current.equals(fight.getCurrentAttacker(0))) {
          fight.switchAttacker();
        }
      }
    }
    System.out.println(
        "Attacker switched: to "
            + fight.getCurrentAttacker(0)
            + " At: "
            + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
            + '\n');

    if (fight instanceof FightPVP) {
      notifServ.sendSwitch(
          ((FightPVP) fight).getFighter1().getLogin(), fight.getCurrentAttacker(0));
      notifServ.sendSwitch(
          ((FightPVP) fight).getFighter2().getLogin(), fight.getCurrentAttacker(0));
      if (fight.getCurrentAttacker(0).length() == 4) {
        animalPvpAttack((FightPVP) fight);
      }
    } else {
      ((FightVsAI) fight)
          .getSetFighters()
          .forEach(
              (user) ->
                  notifServ.sendSwitch(
                      user.getFighter().getUser().getLogin(), fight.getCurrentAttacker(0)));
      if (fight.getCurrentAttacker(0).length() < 3) {
        bossAttack((FightVsAI) fight);
      } else if (fight.getCurrentAttacker(0).length() >= 3
          && fight.getCurrentAttacker(0).length() < 5) {
        animalPveAttack((FightVsAI) fight);
      }
    }
    fightStateStore.saveFight(fight);
    if (fight.getCurrentAttacker(0).length() >= 6) {
      timers.put(
          fight.getId(), scheduler.schedule(() -> schedule(fight, false), 30, TimeUnit.SECONDS));
    }
  }

  private Attack attackPvp(String attackerName, String enemyName, int fightId, String spellName) {
    Attack attack = new Attack();
    FightPVP fight = (FightPVP) fightStateStore.getFight(fightId).orElse(null);
    if (fight == null) {
      attack.setCode(2);
      return attack;
    }
    User attacker = fight.getFighter1();
    User enemy = fight.getFighter2();
    NinjaAnimal targetAnimal = null;
    boolean userIsTarget = true;
    if (enemyName.length() < 6) {
      userIsTarget = false;
      if (enemyName.charAt(3) == 0) targetAnimal = fight.getAnimals2().get(0);
      else targetAnimal = fight.getAnimals1().get(0);
    }
    if (attacker == null || enemy == null) {
      attack.setCode(6);
      return attack;
    }
    if (userIsTarget && enemy.getLogin().equals(attackerName)) {
      User tmp = attacker;
      attacker = enemy;
      enemy = tmp;
    }
    int damage;
    int chakra;
    if (userIsTarget)
      if (!spellName.equalsIgnoreCase("Physical attack")) {
        Spell spell = spellService.get(spellName);
        SpellKnowledge handling =
            SpellKnowledgeService.getSpellKnowledge(attacker.getCharacter(), spell);
        if (spell == null) {
          attack.setCode(8);
          return attack;
        }
        if (spellName.equals("Air Strike")) {
          damage = spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel();
        } else {
          damage =
              Math.round(
                  (spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel())
                      * (1 - enemy.getCharacter().getResistance()));
        }
        if (spellName.equals("Fire Strike") && enemy.getCharacter().getResistance() < 0.8) {
          damage *= 2;
        }
        chakra =
            spell.getBaseChakraConsumption()
                + handling.getSpellLevel() * spell.getChakraConsumptionPerLevel();
      } else {
        damage =
            Math.round(
                attacker.getCharacter().getPhysicalDamage()
                    * (1 - enemy.getCharacter().getResistance()));
        chakra = 0;
      }
    // animal is a target
    else {
      if (!spellName.equalsIgnoreCase("Physical attack")) {
        Spell spell = spellService.get(spellName);
        SpellKnowledge handling =
            SpellKnowledgeService.getSpellKnowledge(attacker.getCharacter(), spell);
        if (spell == null) {
          attack.setCode(8);
          return attack;
        }
        if (spellName.equals("Air Strike")) {
          damage = spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel();
        } else {
          damage =
              Math.round(
                  (spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel())
                      * (1 - targetAnimal.getResistance()));
        }
        if (spellName.equals("Fire Strike") && targetAnimal.getResistance() < 0.8) {
          damage *= 2;
        }
        chakra =
            spell.getBaseChakraConsumption()
                + handling.getSpellLevel() * spell.getChakraConsumptionPerLevel();
      } else {
        damage =
            Math.round(
                attacker.getCharacter().getPhysicalDamage() * (1 - targetAnimal.getResistance()));
        chakra = 0;
      }
    }
    int chakraBurn = 0;
    if (spellName.equals("Water Strike")) {
      chakraBurn = damage / 10;
    }
    attack.setDamage(damage);
    attack.setChakra(chakra);
    attacker.getCharacter().spendChakra(chakra);
    if (userIsTarget) {
      enemy.getCharacter().acceptDamage(damage);
      attack.setDeadly(enemy.getCharacter().getCurrentHP() <= 0);
      sendAfterAttack(
          enemyName,
          damage,
          enemyName,
          attackerName,
          fight.getNextAttacker(),
          attack.isDeadly(),
          attack.isDeadly(),
          spellName,
          chakra,
          chakraBurn);
      sendAfterAttack(
          attackerName,
          damage,
          enemyName,
          attackerName,
          fight.getNextAttacker(),
          attack.isDeadly(),
          attack.isDeadly(),
          spellName,
          chakra,
          chakraBurn);
    } else {
      targetAnimal.acceptDamage(damage);
      attack.setDeadly(targetAnimal.getCurrentHP() <= 0);
      sendAfterAttack(
          fight.getFighter1().getLogin(),
          damage,
          targetAnimal.getName(),
          attackerName,
          fight.getNextAttacker(),
          attack.isDeadly(),
          false,
          spellName,
          chakra,
          0);
      sendAfterAttack(
          fight.getFighter2().getLogin(),
          damage,
          targetAnimal.getName(),
          attackerName,
          fight.getNextAttacker(),
          attack.isDeadly(),
          false,
          spellName,
          chakra,
          0);
    }

    boolean fightFinished = false;
    if (attack.isDeadly()) {
      if (!userIsTarget) {
        if (enemyName.charAt(3) == 1) {
          fight.getAnimals1().clear();
          System.out.println("animal 1 died");
        } else {
          fight.getAnimals2().clear();
          System.out.println("animal 2 died");
        }
      } else {
        if (fight.getFighter1().getLogin().equals(enemyName)) {
          fight.setFirstWon(false);
        } else {
          fight.setFirstWon(true);
        }
        int firstFighterPreviousRating = fight.getFighter1().getStats().getRating();
        int secondFighterPreviousRating = fight.getFighter2().getStats().getRating();
        int rating;
        if (firstFighterPreviousRating >= secondFighterPreviousRating && fight.isFirstWon()
            || secondFighterPreviousRating >= firstFighterPreviousRating && !fight.isFirstWon()) {
          rating = fight.getLessRatingChange();
        } else {
          rating = fight.getBiggerRatingChange();
        }
        fight.setRatingChange(rating);
        if (fight.isFirstWon()) {
          fight
              .getFighter1()
              .getStats()
              .setRating(fight.getFighter1().getStats().getRating() + rating);
          fight.getFighter1().getStats().setFights(fight.getFighter1().getStats().getFights() + 1);
          fight.getFighter1().getStats().setWins(fight.getFighter1().getStats().getWins() + 1);
          fight
              .getFighter2()
              .getStats()
              .setRating(fight.getFighter2().getStats().getRating() - rating);
          fight.getFighter2().getStats().setFights(fight.getFighter2().getStats().getFights() + 1);
          fight.getFighter2().getStats().setLosses(fight.getFighter2().getStats().getLosses() + 1);
        } else {
          fight
              .getFighter1()
              .getStats()
              .setRating(fight.getFighter1().getStats().getRating() - rating);
          fight.getFighter1().getStats().setFights(fight.getFighter1().getStats().getFights() + 1);
          fight.getFighter1().getStats().setLosses(fight.getFighter1().getStats().getLosses() + 1);
          fight
              .getFighter2()
              .getStats()
              .setRating(fight.getFighter2().getStats().getRating() + rating);
          fight.getFighter2().getStats().setFights(fight.getFighter2().getStats().getFights() + 1);
          fight.getFighter2().getStats().setWins(fight.getFighter2().getStats().getWins() + 1);
        }
        statsServ.addStats(fight.getFighter1().getStats());
        statsServ.addStats(fight.getFighter2().getStats());
        fight.setFirstFighter(fight.getFighter1().getCharacter());
        fight.setSecondFighter(fight.getFighter2().getCharacter());
        pvpFightsService.addFight(fight);
        timers.get(fightId).cancel(true);
        System.out.println("FightId: " + fightId + " getId: " + fight.getId());
        timers.remove(fightId);
        fightStateStore.removeFight(fightId);
        fightStateStore.unmarkUserInFight(attackerName);
        fightStateStore.unmarkUserInFight(enemyName);
        fightFinished = true;
      }
    }
    if (!fightFinished) {
      fightStateStore.saveFight(fight);
    }
    return attack;
  }

  private Attack attackPve(String attackerName, int fightId, String spellName) {
    System.out.println(
        "AttackPve at" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
    Attack attack = new Attack();
    FightVsAI fight = (FightVsAI) fightStateStore.getFight(fightId).orElse(null);
    if (fight == null) {
      attack.setCode(2);
      return attack;
    }
    // Boss is a target
    Boss boss = fight.getBoss();
    User attacker = userService.getUser(attackerName);
    int damage;
    int chakra;
    // count damage
    if (!spellName.equalsIgnoreCase("Physical attack")) {
      Spell spell = spellService.get(spellName);
      SpellKnowledge handling =
          SpellKnowledgeService.getSpellKnowledge(attacker.getCharacter(), spell);
      if (spell == null) {
        attack.setCode(8);
        return attack;
      }
      if (spellName.equals("Air Strike")) {
        damage = spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel();
      } else {
        damage =
            Math.round(
                (spell.getBaseDamage() + handling.getSpellLevel() * spell.getDamagePerLevel())
                    * (1 - boss.getResistance()));
      }
      if (spellName.equals("Fire Strike") && boss.getResistance() < 0.8) {
        damage *= 2;
      }
      chakra =
          spell.getBaseChakraConsumption()
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
    List<User> fighters = fight.getFighters();
    // fight.getSetFighters().stream().map(uinF ->
    // uinF.getFighter().getUser()).collect(Collectors.toList());
    // ws
    String nextAtt = fight.getNextAttacker();
    for (User fighter : fighters) {
      sendAfterAttack(
          fighter.getLogin(),
          damage,
          String.valueOf(boss.getNumberOfTails()),
          attacker.getLogin(),
          nextAtt,
          attack.isDeadly(),
          attack.isDeadly(),
          spellName,
          chakra,
          0);
    }
    // if boss was killed
    boolean fightFinished = false;
    if (attack.isDeadly()) {
      fightVsAIService.addFight(fight);
      // set stats and save
      for (AiFightParticipation fightData : fight.getSetFighters()) {
        if (fightData.getResult() == null) fightData.setResult(AiFightParticipation.Result.WON);
        int experience = 500 + 200 * boss.getNumberOfTails();
        if (fightData.getResult().equals(AiFightParticipation.Result.DIED)) {
          experience /= 2;
          fightData
              .getFighter()
              .getUser()
              .getStats()
              .setFights(fightData.getFighter().getUser().getStats().getFights() + 1);
          fightData
              .getFighter()
              .getUser()
              .getStats()
              .setDeaths(fightData.getFighter().getUser().getStats().getDeaths() + 1);
          fightData.getFighter().changeXP(experience);
          statsServ.addStats(fightData.getFighter().getUser().getStats());
        } else {
          fightData
              .getFighter()
              .getUser()
              .getStats()
              .setFights(fightData.getFighter().getUser().getStats().getFights() + 1);
          fightData
              .getFighter()
              .getUser()
              .getStats()
              .setWins(fightData.getFighter().getUser().getStats().getWins() + 1);
          fightData.getFighter().changeXP(experience);
          statsServ.addStats(fightData.getFighter().getUser().getStats());
        }
        fightData.setExperience(experience);
        userAiFightService.add(fightData);
      }
      // close fight
      fightStateStore.removeQueue(fightId);
      for (AiFightParticipation fighter : fight.getSetFighters()) {
        fightStateStore.unmarkUserInFight(fighter.getFighter().getUser().getLogin());
      }
      fightStateStore.removeFight(fight.getId());
      fightFinished = true;
    }
    if (!fightFinished) {
      fightStateStore.saveFight(fight);
    }
    return attack;
  }

  private void bossAttack(FightVsAI fight) {
    timers.get(fight.getId()).cancel(true);
    // time for attack
    int delay = (int) (Math.random() * 7000) + 500;
    // targetNum - first fighters.size() - from fighters, next from animals
    int targetNum =
        (int) (Math.random() * (fight.getFighters().size() + fight.getAnimals1().size() - 0.5));
    boolean targetUser = targetNum < fight.getFighters().size();
    // null if target is not a user
    User target = targetUser ? fight.getFighters().get(targetNum) : null;
    // null if target is not an animal
    NinjaAnimal targetAnimal =
        targetUser ? null : fight.getAnimals1().get(targetNum - fight.getFighters().size());

    int damage =
        (int)
            Math.round(
                30
                    * Math.pow(fight.getBoss().getNumberOfTails(), 1.5)
                    * (targetUser
                        ? (1 - target.getCharacter().getResistance())
                        : (1 - targetAnimal.getResistance())));
    // target gets damage

    // send attack after delay and continue timer
    timers.put(
        fight.getId(),
        scheduler.schedule(
            () -> {
              boolean deadly;
              if (targetUser) {
                target.getCharacter().acceptDamage(damage);
                System.out.println(
                    "Boss Attack at"
                        + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
                        + "Damage: "
                        + damage
                        + "Current: "
                        + target.getCharacter().getCurrentHP()
                        + '\n');
                deadly = target.getCharacter().getCurrentHP() <= 0;
                if (deadly) {
                  for (int i = 0; i < fight.getFighters().size(); i++) {
                    if (fight.getFighters().get(i).getLogin().equals(target.getLogin())) {
                      System.out.println(
                          "Killed: " + fight.getFighters().remove(i).getLogin() + '\n');
                      break;
                    }
                  }
                  fight
                      .getSetFighters()
                      .forEach(
                          (set) -> {
                            if (set.getFighter().getUser().getLogin().equals(target.getLogin()))
                              set.setResult(AiFightParticipation.Result.DIED);
                          });
                  System.out.print("Remaining: ");
                }
                fight.getFighters().forEach((user) -> System.out.print(user.getLogin() + " "));
              } else {
                targetAnimal.acceptDamage(damage);
                deadly = targetAnimal.getCurrentHP() <= 0;
                if (deadly) {
                  fight
                      .getAnimals1()
                      .forEach(
                          (an) -> {
                            if (an.getName().equalsIgnoreCase(targetAnimal.getName()))
                              fight.getAnimals1().remove(an);
                          });
                }
              }
              // check if everyone is dead
              boolean allDead = true;
              for (User user : fight.getFighters()) {
                if (user.getCharacter().getCurrentHP() > 0) allDead = false;
              }
              System.out.println("Are all dead: " + allDead + "\n");
              // because lambda needs (effectively) final variables
              final boolean everyoneDied = allDead;

              fight
                  .getSetFighters()
                  .forEach(
                      (fighter) ->
                          sendAfterAttack(
                              fighter.getFighter().getUser().getLogin(),
                              damage,
                              targetUser
                                  ? target.getLogin()
                                  : targetAnimal.getName().substring(0, 3),
                              fight.getCurrentAttacker(0),
                              fight.getNextAttacker(),
                              deadly,
                              everyoneDied,
                              "Boss attack",
                              0,
                              0));

              if (allDead) {
                System.out.println("fight ended\n");
                fightVsAIService.addFight(fight);
                for (AiFightParticipation userData : fight.getSetFighters()) {
                  userData.setExperience(50);
                  userData.setResult(AiFightParticipation.Result.LOST);
                  userData
                      .getFighter()
                      .getUser()
                      .getStats()
                      .setFights(userData.getFighter().getUser().getStats().getFights() + 1);
                  userData
                      .getFighter()
                      .getUser()
                      .getStats()
                      .setLosses(userData.getFighter().getUser().getStats().getLosses() + 1);
                  userData.getFighter().changeXP(50);
                  statsServ.addStats(userData.getFighter().getUser().getStats());
                  userAiFightService.add(userData);
                }
                fightStateStore.removeQueue(fight.getId());
                for (AiFightParticipation fighter : fight.getSetFighters()) {
                  fightStateStore.unmarkUserInFight(fighter.getFighter().getUser().getLogin());
                }
                timers.get(fight.getId()).cancel(true);
                fightStateStore.removeFight(fight.getId());
                return;
              }
              System.out.println("YEEEE!\n");
              fightStateStore.saveFight(fight);
              schedule(fight, false);
            },
            delay,
            TimeUnit.MILLISECONDS));
  }

  private void animalPvpAttack(FightPVP fight) {
    String animName = fight.getCurrentAttacker(0).substring(0, 3);
    boolean fromAnimals1 = animName.charAt(3) == '1';
    NinjaAnimal attacker;
    switch (animName) {
      case "Дяд":
        {
          attacker = ninjaAnimalService.findByName("Дядя Бафомет");
          break;
        }
      case "Тёт":
        {
          attacker = ninjaAnimalService.findByName("Тётя Срака");
          break;
        }
      case "Ube":
        {
          attacker = ninjaAnimalService.findByName("Ubele");
          break;
        }
      case "Ver":
        {
          attacker = ninjaAnimalService.findByName("Vertet");
          break;
        }
      case "Lus":
        {
          attacker = ninjaAnimalService.findByName("Lusis");
          break;
        }
      case "Lau":
        {
          attacker = ninjaAnimalService.findByName("Lauva");
          break;
        }
      case "Lap":
        {
          attacker = ninjaAnimalService.findByName("Lapsa");
          break;
        }
      default:
        {
          attacker = ninjaAnimalService.findByName("Erglis");
        }
    }

    timers.get(fight.getId()).cancel(true);

    int delay = (int) (Math.random() * 7000) + 500;
    User target;
    NinjaAnimal targetAnimal;
    boolean targetUser;
    // enemy animal attacks
    if (!fromAnimals1) {
      int targetNum =
          (int)
              Math.round(
                  Math.random() * fight.getAnimals1().size()); // 0 - usertarget, 1 -animaltarget
      targetUser = targetNum == 0;
      target = targetUser ? fight.getFighter1() : null;
      targetAnimal =
          targetUser ? null : fight.getAnimals1().get(targetNum - fight.getAnimals1().size());
    }
    // your animal attacks
    else {
      int targetNum = (int) Math.round(Math.random() * fight.getAnimals2().size());
      targetUser = targetNum == 0;
      target = targetUser ? fight.getFighter2() : null;
      targetAnimal =
          targetUser ? null : fight.getAnimals2().get(targetNum - fight.getAnimals2().size());
    }
    timers.put(
        fight.getId(),
        scheduler.schedule(
            () -> {
              // damage
              boolean deadly = false;
              int damage = attacker.getDamage();
              if (targetUser) {
                damage *= (1 - target.getCharacter().getResistance());
                target.getCharacter().acceptDamage(damage);
                if (target.getCharacter().getCurrentHP() <= 0) deadly = true;
              } else {
                damage *= (1 - targetAnimal.getResistance());
                targetAnimal.acceptDamage(damage);
                if (targetAnimal.getCurrentHP() <= 0) deadly = true;
              }
              boolean finish = targetUser && deadly;
              sendAfterAttack(
                  fight.getFighter1().getLogin(),
                  damage,
                  targetUser ? target.getLogin() : targetAnimal.getName(),
                  fight.getCurrentAttacker(0),
                  fight.getNextAttacker(),
                  deadly,
                  finish,
                  "Physical attack",
                  0,
                  0);
              sendAfterAttack(
                  fight.getFighter2().getLogin(),
                  damage,
                  targetUser ? target.getLogin() : targetAnimal.getName(),
                  fight.getCurrentAttacker(0),
                  fight.getNextAttacker(),
                  deadly,
                  finish,
                  "Physical attack",
                  0,
                  0);
              // if animal died
              if (deadly && !finish) {
                if (fromAnimals1) {
                  fight.getAnimals2().clear();
                  System.out.println("animal2 died");
                } else {
                  fight.getAnimals1().clear();
                  System.out.println("animal1 died");
                }
              }
              // if user died
              if (finish) {
                if (fromAnimals1) {
                  fight.setFirstWon(true);
                } else {
                  fight.setFirstWon(false);
                }
                int firstFighterPreviousRating = fight.getFighter1().getStats().getRating();
                int secondFighterPreviousRating = fight.getFighter2().getStats().getRating();
                int rating;
                if (firstFighterPreviousRating >= secondFighterPreviousRating && fight.isFirstWon()
                    || secondFighterPreviousRating >= firstFighterPreviousRating
                        && !fight.isFirstWon()) {
                  rating = fight.getLessRatingChange();
                } else {
                  rating = fight.getBiggerRatingChange();
                }
                fight.setRatingChange(rating);
                if (fight.isFirstWon()) {
                  fight
                      .getFighter1()
                      .getStats()
                      .setRating(fight.getFighter1().getStats().getRating() + rating);
                  fight
                      .getFighter1()
                      .getStats()
                      .setFights(fight.getFighter1().getStats().getFights() + 1);
                  fight
                      .getFighter1()
                      .getStats()
                      .setWins(fight.getFighter1().getStats().getWins() + 1);
                  fight
                      .getFighter2()
                      .getStats()
                      .setRating(fight.getFighter2().getStats().getRating() - rating);
                  fight
                      .getFighter2()
                      .getStats()
                      .setFights(fight.getFighter2().getStats().getFights() + 1);
                  fight
                      .getFighter2()
                      .getStats()
                      .setLosses(fight.getFighter2().getStats().getLosses() + 1);
                } else {
                  fight
                      .getFighter1()
                      .getStats()
                      .setRating(fight.getFighter1().getStats().getRating() - rating);
                  fight
                      .getFighter1()
                      .getStats()
                      .setFights(fight.getFighter1().getStats().getFights() + 1);
                  fight
                      .getFighter1()
                      .getStats()
                      .setLosses(fight.getFighter1().getStats().getLosses() + 1);
                  fight
                      .getFighter2()
                      .getStats()
                      .setRating(fight.getFighter2().getStats().getRating() + rating);
                  fight
                      .getFighter2()
                      .getStats()
                      .setFights(fight.getFighter2().getStats().getFights() + 1);
                  fight
                      .getFighter2()
                      .getStats()
                      .setWins(fight.getFighter2().getStats().getWins() + 1);
                }
                statsServ.addStats(fight.getFighter1().getStats());
                statsServ.addStats(fight.getFighter2().getStats());
                fight.setFirstFighter(fight.getFighter1().getCharacter());
                fight.setSecondFighter(fight.getFighter2().getCharacter());
                pvpFightsService.addFight(fight);
                timers.get(fight.getId()).cancel(true);
                // System.out.println("FightId: " + fightId + " getId: " + fight.getId());
                timers.remove(fight.getId());
                fightStateStore.removeFight(fight.getId());
                fightStateStore.unmarkUserInFight(fight.getFighter1().getLogin());
                fightStateStore.unmarkUserInFight(fight.getFighter2().getLogin());
                return;
              }
              fightStateStore.saveFight(fight);
              schedule(fight, false);
            },
            delay,
            TimeUnit.MILLISECONDS));
  }

  @PostMapping("/summonPvp")
  public ResponseEntity summonPvp(@RequestParam int fightId) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userService.getUser(name);
    FightPVP fight = (FightPVP) fightStateStore.getFight(fightId).orElse(null);
    if (fight == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\"code\": 2,\"error\":\"Fight doesn't exist\"}");
    }
    NinjaAnimalRace race = user.getCharacter().getAnimalRace();
    if (race != null) {
      boolean lvl2 = user.getCharacter().getLevel() >= 10;
      String animalName;
      switch (race) {
        case Bugurt:
          animalName = lvl2 ? "Дядя Бафомет" : "Тётя Срака";
          break;
        case Veseliba:
          animalName = lvl2 ? "Ubele" : "Vertet";
          break;
        case Bojajumus:
          animalName = lvl2 ? "Lusis" : "Lauva";
          break;
        default:
          animalName = lvl2 ? "Lapsa" : "Erglis";
          break;
      }
      NinjaAnimal animal = ninjaAnimalService.findByName(animalName);
      animal.prepareForFight();
      if (fight.getFighter1().getLogin().equals(name)) {
        fight.getAnimals1().add(animal);
        notifServ.sendSummon(fight.getFighter2().getLogin(), name, animal, animalName);
      } else {
        fight.getAnimals2().add(animal);
        notifServ.sendSummon(fight.getFighter1().getLogin(), name, animal, animalName);
      }
      fightStateStore.saveFight(fight);
      return ResponseEntity.ok(animal.toString());
    } else
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("{\"answer\":\"You haven't chosen your animal\"}");
  }

  @PostMapping("/summonPve")
  public ResponseEntity summonPve(@RequestParam int fightId) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userService.getUser(name);
    FightVsAI fight = (FightVsAI) fightStateStore.getFight(fightId).orElse(null);
    if (fight == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\"code\": 2,\"error\":\"Fight doesn't exist\"}");
    }
    NinjaAnimalRace race = user.getCharacter().getAnimalRace();
    boolean lvl2 = user.getCharacter().getLevel() >= 10;
    String animalName;
    switch (race) {
      case Bugurt:
        animalName = lvl2 ? "Дядя Бафомет" : "Тётя Срака";
        break;
      case Veseliba:
        animalName = lvl2 ? "Ubele" : "Vertet";
        break;
      case Bojajumus:
        animalName = lvl2 ? "Lusis" : "Lauva";
        break;
      default:
        animalName = lvl2 ? "Lapsa" : "Erglis";
        break;
    }
    NinjaAnimal animal = ninjaAnimalService.findByName(animalName);
    animal.prepareForFight();
    fight.getAnimals1().add(animal);
    fight
        .getSetFighters()
        .forEach(
            (set) -> {
              if (!set.getFighter().getUser().getLogin().equals(name))
                notifServ.sendSummon(
                    set.getFighter().getUser().getLogin(), name, animal, animalName);
            });

    fightStateStore.saveFight(fight);
    return ResponseEntity.ok(animal.toString());
  }

  private void animalPveAttack(FightVsAI fight) {
    System.out.println("Animal attack");
    String animName = fight.getCurrentAttacker(0);
    NinjaAnimal attacker = null;
    switch (animName) {
      case "Дяд":
        {
          attacker = ninjaAnimalService.findByName("Дядя Бафомет");
          break;
        }
      case "Тёт":
        {
          attacker = ninjaAnimalService.findByName("Тётя Срака");
          break;
        }
      case "Ube":
        {
          attacker = ninjaAnimalService.findByName("Ubele");
          break;
        }
      case "Ver":
        {
          attacker = ninjaAnimalService.findByName("Vertet");
          break;
        }
      case "Lus":
        {
          attacker = ninjaAnimalService.findByName("Lusis");
          break;
        }
      case "Lau":
        {
          attacker = ninjaAnimalService.findByName("Lauva");
          break;
        }
      case "Lap":
        {
          attacker = ninjaAnimalService.findByName("Lapsa");
          break;
        }
      default:
        {
          attacker = ninjaAnimalService.findByName("Erglis");
        }
    }

    timers.get(fight.getId()).cancel(true);

    int delay = (int) (Math.random() * 7000) + 500;
    Boss target = fight.getBoss();
    int damage = Math.round(attacker.getDamage() * (1 - target.getResistance()));
    timers.put(
        fight.getId(),
        scheduler.schedule(
            () -> {
              target.acceptDamage(damage);
              boolean deadly = false;
              if (target.getCurrentHP() <= 0) deadly = true;
              final boolean dead = deadly;
              fight
                  .getSetFighters()
                  .forEach(
                      (set) -> {
                        sendAfterAttack(
                            set.getFighter().getUser().getLogin(),
                            damage,
                            String.valueOf(target.getNumberOfTails()),
                            fight.getCurrentAttacker(0),
                            fight.getNextAttacker(),
                            dead,
                            dead,
                            "Physical attack",
                            0,
                            0);
                      });
              if (deadly) {
                fightVsAIService.addFight(fight);
                // set stats and save
                for (AiFightParticipation fightData : fight.getSetFighters()) {
                  if (fightData.getResult() == null)
                    fightData.setResult(AiFightParticipation.Result.WON);
                  int experience = 500 + 200 * target.getNumberOfTails();
                  if (fightData.getResult().equals(AiFightParticipation.Result.DIED)) {
                    experience /= 2;
                    fightData
                        .getFighter()
                        .getUser()
                        .getStats()
                        .setFights(fightData.getFighter().getUser().getStats().getFights() + 1);
                    fightData
                        .getFighter()
                        .getUser()
                        .getStats()
                        .setDeaths(fightData.getFighter().getUser().getStats().getDeaths() + 1);
                    fightData.getFighter().changeXP(experience);
                    statsServ.addStats(fightData.getFighter().getUser().getStats());
                  } else {
                    fightData
                        .getFighter()
                        .getUser()
                        .getStats()
                        .setFights(fightData.getFighter().getUser().getStats().getFights() + 1);
                    fightData
                        .getFighter()
                        .getUser()
                        .getStats()
                        .setWins(fightData.getFighter().getUser().getStats().getWins() + 1);
                    fightData.getFighter().changeXP(experience);
                    statsServ.addStats(fightData.getFighter().getUser().getStats());
                  }
                  fightData.setExperience(experience);
                  userAiFightService.add(fightData);
                }
                // close fight
                fightStateStore.removeQueue(fight.getId());
                for (AiFightParticipation fighter : fight.getSetFighters()) {
                  fightStateStore.unmarkUserInFight(fighter.getFighter().getUser().getLogin());
                }
                timers.get(fight.getId()).cancel(true);
                fightStateStore.removeFight(fight.getId());
                return;
              }
              fightStateStore.saveFight(fight);
              schedule(fight, false);
            },
            delay,
            TimeUnit.MILLISECONDS));
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
   * @param username Receiver of a message on websocket
   * @param damage
   * @param targetName Username of the target
   * @param attacker Username of the attacker
   * @param next Username of the next cha
   * @param dead
   * @param allDead
   * @param attackName
   * @param chakraCost
   * @param chakraBurn
   */
  private void sendAfterAttack(
      String username,
      int damage,
      String targetName,
      String attacker,
      String next,
      boolean dead,
      boolean allDead,
      String attackName,
      int chakraCost,
      int chakraBurn) {
    State state =
        new State(
            attacker, targetName, attackName, chakraCost, damage, chakraBurn, dead, allDead, next);
    notifServ.sendFightState(state, username);
  }
}
