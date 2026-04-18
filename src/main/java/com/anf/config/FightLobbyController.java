package com.anf.config;

import com.anf.service.FightLobbyService;
import com.anf.service.FightStartService;
import com.anf.service.FightTurnEngineService;
import lombok.RequiredArgsConstructor;
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
public class FightLobbyController {

  private final FightLobbyService fightLobbyService;
  private final FightStartService fightStartService;
  private final FightTurnEngineService fightTurnEngineService;

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
}
