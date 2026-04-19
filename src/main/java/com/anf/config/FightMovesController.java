package com.anf.config;

import com.anf.model.Fight;
import com.anf.service.FightAttackService;
import com.anf.service.FightSnapshotService;
import com.anf.service.FightSummonService;
import com.anf.service.FightTurnEngineService;
import com.anf.service.PveAttackService;
import com.anf.service.PvpAttackService;
import com.anf.service.state.FightRuntimeStore;
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
  private final FightSummonService fightSummonService;
  private final FightTurnEngineService fightTurnEngineService;
  private final PvpAttackService pvpAttackService;
  private final PveAttackService pveAttackService;
  private final FightRuntimeStore fightStateStore;

  @PostMapping("info")
  public ResponseEntity info(@RequestParam String fightUuid) {
    if (!fightSnapshotService.hasProtobufState(fightUuid)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\n\"code\": 2\n}");
    }
    Fight fight = fightStateStore.getFight(fightUuid).orElse(null);
    if (fight == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\n\"code\": 2\n}"); // code 2 means fight doesn't exist
    }
    var turnStartedAt = fightSnapshotService.currentTurnStartedAt(fightUuid);
    if (turnStartedAt > 0) {
      var remaining = Math.max(0L, 30_000L - (System.currentTimeMillis() - turnStartedAt));
      fight.setTimeLeft(remaining);
    }
    fightStateStore.saveFight(fightUuid, fight);
    return ResponseEntity.status(HttpStatus.OK).body(fight.toString());
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
}
