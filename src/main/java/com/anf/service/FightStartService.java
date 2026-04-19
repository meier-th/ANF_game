package com.anf.service;

import com.anf.configuration.WebSocketsController;
import com.anf.service.fight.model.Fight;
import com.anf.infrastructure.state.FightRuntimeFacade;
import com.anf.infrastructure.state.FightRuntimeStore;
import com.anf.service.state.proto.GameStateModels.FightMode;
import java.util.Map;
import java.util.function.BiConsumer;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FightStartService {
  private final FightRuntimeFacade fightRuntimeFacade;
  private final FightLobbyService fightLobbyService;
  private final FightRuntimeFactoryService fightRuntimeFactoryService;
  private final FightRuntimeStore fightStateStore;
  private final FightSnapshotService fightSnapshotService;
  private final WebSocketsController webSocketsController;

  public ResponseEntity<?> startFightFromLobby(
      String lobbyUuid, String bossName, String requester, BiConsumer<Fight, String> onFirstTurn) {
    var lobby = fightRuntimeFacade.getLobby(lobbyUuid);
    if (lobby.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("code", 2, "error", "Lobby doesn't exist"));
    }

    var result = fightRuntimeFacade.startFightFromLobby(lobbyUuid);
    if (result.status() != FightRuntimeFacade.StartFightResultStatus.STARTED) {
      return fightLobbyService.mapStartFightFailure(result);
    }

    var participants = lobby.get().getPlayerIdsList();
    if (lobby.get().getFightMode() == FightMode.FIGHT_MODE_PVP) {
      var runtimeFight = fightRuntimeFactoryService.createPvpRuntimeFight(participants);
      if (runtimeFight == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{ \"code\": 3}");
      }
      var fightUuid = result.fight().getFightUuid();
      fightStateStore.saveFight(fightUuid, runtimeFight);
      fightStateStore.markUserInFight(runtimeFight.getFighter1().getLogin());
      fightStateStore.markUserInFight(runtimeFight.getFighter2().getLogin());
      fightSnapshotService.syncFightSnapshot(fightUuid, runtimeFight);

      var opponent =
          requester.equals(runtimeFight.getFighter1().getLogin())
              ? runtimeFight.getFighter2().getLogin()
              : runtimeFight.getFighter1().getLogin();
      webSocketsController.sendStart(requester, opponent, fightUuid);
      onFirstTurn.accept(runtimeFight, fightUuid);
      return buildCreatedResponse(fightUuid, result.fight().getFightMode().name(), result.fight().getParticipantUuidsList());
    }

    if (bossName == null || bossName.isBlank()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("code", 8, "error", "bossId is required for PvE fights"));
    }
    var runtimeFight = fightRuntimeFactoryService.createPveRuntimeFight(participants, bossName);
    if (runtimeFight == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{ \"code\": 3}");
    }

    var fightUuid = result.fight().getFightUuid();
    fightStateStore.markUsersInFight(participants);
    fightStateStore.saveFight(fightUuid, runtimeFight);
    fightSnapshotService.syncFightSnapshot(fightUuid, runtimeFight);
    participants.forEach(
        (user) -> {
          if (!user.equals(requester)) {
            webSocketsController.sendStart(requester, user, fightUuid);
          }
        });
    onFirstTurn.accept(runtimeFight, fightUuid);
    return buildCreatedResponse(fightUuid, result.fight().getFightMode().name(), result.fight().getParticipantUuidsList());
  }

  private ResponseEntity<?> buildCreatedResponse(String fightUuid, String fightMode, java.util.List<String> participants) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .header(HttpHeaders.LOCATION, "/fight/" + fightUuid)
        .body(Map.of("fightUuid", fightUuid, "fightMode", fightMode, "participants", participants));
  }
}
