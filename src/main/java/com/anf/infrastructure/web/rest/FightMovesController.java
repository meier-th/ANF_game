package com.anf.infrastructure.web.rest;

import com.anf.domain.fight.model.Fight;
import com.anf.domain.shared.ApiField;
import com.anf.domain.shared.ErrorCode;
import com.anf.domain.fight.FightAttackService;
import com.anf.domain.fight.FightSnapshotService;
import com.anf.domain.fight.FightSurrenderService;
import com.anf.domain.fight.FightSummonService;
import com.anf.domain.fight.FightTurnEngineService;
import com.anf.domain.combat.PveAttackService;
import com.anf.domain.combat.PvpAttackService;
import com.anf.domain.combat.SpellKnowledgeService;
import com.anf.domain.user.UserService;
import com.anf.infrastructure.state.FightRuntimeStore;
import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.SpellKnowledge;
import com.anf.model.database.User;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
public class FightMovesController {

  private final FightAttackService fightAttackService;
  private final FightSnapshotService fightSnapshotService;
  private final FightSurrenderService fightSurrenderService;
  private final FightSummonService fightSummonService;
  private final FightTurnEngineService fightTurnEngineService;
  private final PvpAttackService pvpAttackService;
  private final PveAttackService pveAttackService;
  private final FightRuntimeStore fightStateStore;
  private final UserService userService;
  private final SpellKnowledgeService spellKnowledgeService;

  @PostMapping("/info")
  public ResponseEntity info(@RequestParam String fightUuid) {
    if (!fightSnapshotService.hasProtobufState(fightUuid)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\n\"" + ApiField.CODE.getValue() + "\": " + ErrorCode.NOT_FOUND.getValue() + "\n}");
    }
    Fight fight = fightStateStore.getFight(fightUuid).orElse(null);
    if (fight == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\n\"" + ApiField.CODE.getValue() + "\": " + ErrorCode.NOT_FOUND.getValue() + "\n}");
    }
    var turnStartedAt = fightSnapshotService.currentTurnStartedAt(fightUuid);
    if (turnStartedAt > 0) {
      var remaining = Math.max(0L, 30_000L - (System.currentTimeMillis() - turnStartedAt));
      fight.setTimeLeft(remaining);
    }
    if (fight instanceof FightPVP pvpFight) {
      var currentName = pvpFight.getCurrentAttacker(0);
      if (currentName == null) {
        currentName = "";
      }
      var response = new LinkedHashMap<String, Object>();
      response.put("id", pvpFight.getId());
      response.put("type", "pvp");
      response.put("fighters1", buildFightUserPayload(pvpFight.getFighter1()));
      response.put("fighters2", buildFightUserPayload(pvpFight.getFighter2()));
      response.put("animals1", pvpFight.getAnimals1());
      response.put("animals2", pvpFight.getAnimals2());
      response.put("currentName", currentName);
      response.put("timeLeft", pvpFight.getTimeLeft());
      return ResponseEntity.status(HttpStatus.OK)
          .body(response);
    }
    if (fight instanceof FightVsAI pveFight) {
      var currentName = pveFight.getCurrentAttacker(0);
      if (currentName == null) {
        currentName = "";
      }
      var fightersPayload = new ArrayList<Map<String, Object>>();
      for (var fighter : pveFight.getFighters()) {
        fightersPayload.add(buildFightUserPayload(fighter));
      }
      var response = new LinkedHashMap<String, Object>();
      response.put("id", pveFight.getId());
      response.put("type", "pve");
      response.put("fighters1", fightersPayload);
      response.put("boss", pveFight.getBoss());
      response.put("animals1", pveFight.getAnimals1());
      response.put("currentName", currentName);
      response.put("timeLeft", pveFight.getTimeLeft());
      return ResponseEntity.status(HttpStatus.OK)
          .body(response);
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("{\n\"" + ApiField.CODE.getValue() + "\": " + ErrorCode.INVALID_REQUEST.getValue() + "\n}");
  }

  private Map<String, Object> buildFightUserPayload(User runtimeUser) {
    var payload = new LinkedHashMap<String, Object>();
    if (runtimeUser == null) {
      return payload;
    }
    payload.put("login", runtimeUser.getLogin());
    payload.put("character", buildFightCharacterPayload(runtimeUser));
    return payload;
  }

  private Map<String, Object> buildFightCharacterPayload(User runtimeUser) {
    var characterPayload = new LinkedHashMap<String, Object>();
    var runtimeCharacter = runtimeUser.getCharacter();
    if (runtimeCharacter == null) {
      return characterPayload;
    }

    var persistedUser = userService.getUser(runtimeUser.getLogin());
    var persistedCharacter = persistedUser != null ? persistedUser.getCharacter() : null;
    List<SpellKnowledge> knownSpells =
        persistedCharacter != null
            ? spellKnowledgeService.ensureUnlockedSpellKnowledge(persistedCharacter)
            : List.of();
    var appearance =
        runtimeCharacter.getAppearance() != null
            ? runtimeCharacter.getAppearance()
            : persistedCharacter != null ? persistedCharacter.getAppearance() : null;

    characterPayload.put("currentHP", runtimeCharacter.getCurrentHP());
    characterPayload.put("currentChakra", runtimeCharacter.getCurrentChakra());
    characterPayload.put("maxHp", runtimeCharacter.getMaxHp());
    characterPayload.put("maxChakra", runtimeCharacter.getMaxChakra());
    characterPayload.put("physicalDamage", runtimeCharacter.getPhysicalDamage());
    characterPayload.put("animalRace", runtimeCharacter.getAnimalRace());

    var appearancePayload = new LinkedHashMap<String, Object>();
    if (appearance != null) {
      appearancePayload.put("gender", appearance.getGender());
      appearancePayload.put("hairColour", appearance.getHairColour());
      appearancePayload.put("skinColour", appearance.getSkinColour());
      appearancePayload.put("clothesColour", appearance.getClothesColour());
    }
    characterPayload.put("appearance", appearancePayload);

    var spellsPayload = new ArrayList<Map<String, Object>>();
    for (var knownSpell : knownSpells) {
      var knownSpellPayload = new LinkedHashMap<String, Object>();
      knownSpellPayload.put("spellLevel", knownSpell.getSpellLevel());

      var spellPayload = new LinkedHashMap<String, Object>();
      spellPayload.put("name", knownSpell.getSpellUse().getName());
      spellPayload.put("baseDamage", knownSpell.getSpellUse().getBaseDamage());
      spellPayload.put("baseChakraConsumption", knownSpell.getSpellUse().getBaseChakraConsumption());
      spellPayload.put("damagePerLevel", knownSpell.getSpellUse().getDamagePerLevel());
      spellPayload.put("chakraConsumptionPerLevel", knownSpell.getSpellUse().getChakraConsumptionPerLevel());
      knownSpellPayload.put("spellUse", spellPayload);

      spellsPayload.add(knownSpellPayload);
    }
    characterPayload.put("spellsKnown", spellsPayload);

    return characterPayload;
  }

  @RequestMapping("/attack")
  public ResponseEntity<?> attackHandler(
      @RequestParam String enemy, @RequestParam String fightUuid, @RequestParam String spellName) {
    var name = SecurityContextHolder.getContext().getAuthentication().getName();
    var context = new FightAttackService.AttackContext(name, enemy, fightUuid, spellName);
    return fightAttackService.attack(
        context,
        (ctx) -> pvpAttackService.attackPvp(ctx.attackerName(), ctx.enemyName(), ctx.fightUuid(), ctx.spellName()),
        (ctx) -> pveAttackService.attackPve(ctx.attackerName(), ctx.fightUuid(), ctx.spellName()),
        () ->
            fightStateStore
                .getFight(fightUuid)
                .ifPresent((fight) -> fightTurnEngineService.schedule(fight, fightUuid, false)));
  }

  @PostMapping("/summonPvp")
  public ResponseEntity summonPvp(@RequestParam String fightUuid) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    return fightSummonService.summonPvp(fightUuid, name);
  }

  @PostMapping("/summonPve")
  public ResponseEntity summonPve(@RequestParam String fightUuid) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    return fightSummonService.summonPve(fightUuid, name);
  }

  @PostMapping("/timeout")
  public ResponseEntity<?> timeoutCurrentTurn(
      @RequestParam String fightUuid, @RequestParam String timedOutAttacker) {
    var reporter = SecurityContextHolder.getContext().getAuthentication().getName();
    return fightTurnEngineService.timeoutCurrentTurn(fightUuid, reporter, timedOutAttacker);
  }

  @PostMapping("/surrender")
  public ResponseEntity<?> surrender(@RequestParam String fightUuid) {
    var username = SecurityContextHolder.getContext().getAuthentication().getName();
    return fightSurrenderService.surrender(fightUuid, username);
  }
}
