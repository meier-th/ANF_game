package com.anf.config;

import com.anf.model.Attack;
import com.anf.model.Fight;
import com.anf.service.FightAttackService;
import com.anf.service.FightLobbyService;
import com.anf.service.FightSnapshotService;
import com.anf.service.FightStartService;
import com.anf.service.FightSummonService;
import com.anf.service.FightTurnEngineService;
import com.anf.service.InMemoryFightTurnScheduler;
import com.anf.service.PveAttackService;
import com.anf.service.PvpAttackService;
import com.anf.service.state.LegacyFightRuntimeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fight")
@RequiredArgsConstructor
public class FightController {

  private final FightAttackService fightAttackService;
  private final FightLobbyService fightLobbyService;
  private final FightSnapshotService fightSnapshotService;
  private final FightStartService fightStartService;
  private final FightSummonService fightSummonService;
  private final FightTurnEngineService fightTurnEngineService;
  private final InMemoryFightTurnScheduler fightTurnScheduler;
  private final PvpAttackService pvpAttackService;
  private final PveAttackService pveAttackService;
  private final LegacyFightRuntimeStore fightStateStore;

  @PostMapping("/lobbies")
  public ResponseEntity<?> createLobby(@RequestParam(name = "mode") String mode) {
    var leader = SecurityContextHolder.getContext().getAuthentication().getName();
    return fightLobbyService.createLobby(mode, leader);
  }

  @GetMapping("/lobbies/{lobbyUuid}")
  public ResponseEntity<?> getLobby(@PathVariable String lobbyUuid) {
    return fightLobbyService.getLobby(lobbyUuid);
  }

  @PostMapping("/lobbies/{lobbyUuid}/join")
  public ResponseEntity<?> joinLobby(@PathVariable String lobbyUuid) {
    var player = SecurityContextHolder.getContext().getAuthentication().getName();
    return fightLobbyService.joinLobby(lobbyUuid, player);
  }

  @PostMapping("/lobbies/{lobbyUuid}/leave")
  public ResponseEntity<?> leaveLobby(@PathVariable String lobbyUuid) {
    var player = SecurityContextHolder.getContext().getAuthentication().getName();
    return fightLobbyService.leaveLobby(lobbyUuid, player);
  }

  @DeleteMapping("/lobbies/{lobbyUuid}")
  public ResponseEntity<?> closeLobbyV2(@PathVariable String lobbyUuid) {
    fightLobbyService.closeLobby(lobbyUuid);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/lobbies/{lobbyUuid}/start")
  public ResponseEntity<?> startFightFromLobby(
      @PathVariable String lobbyUuid, @RequestParam(name = "bossId", required = false) String bossName) {
    var requester = SecurityContextHolder.getContext().getAuthentication().getName();
    return fightStartService.startFightFromLobby(
        lobbyUuid,
        bossName,
        requester,
        (fight, fightUuid) -> fightTurnEngineService.schedule(fight, fightUuid, true));
  }

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
    fightTurnScheduler.delayMillis(fightUuid).ifPresent(fight::setTimeLeft);
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

}
