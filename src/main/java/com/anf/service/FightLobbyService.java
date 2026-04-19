package com.anf.service;

import com.anf.infrastructure.state.FightRuntimeFacade;
import com.anf.infrastructure.state.LobbyStore;
import com.anf.service.state.proto.GameStateModels.FightMode;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FightLobbyService {
  private final FightRuntimeFacade fightRuntimeFacade;

  public ResponseEntity<?> createLobby(String modeRaw, String leader) {
    var parsedMode = parseFightMode(modeRaw);
    if (parsedMode == FightMode.FIGHT_MODE_UNSPECIFIED) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("code", 8, "error", "Unsupported fight mode"));
    }
    var lobby = fightRuntimeFacade.createLobby(parsedMode, leader);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            Map.of(
                "lobbyUuid", lobby.getLobbyUuid(),
                "fightMode", lobby.getFightMode().name(),
                "leader", lobby.getLeaderPlayerId(),
                "players", lobby.getPlayerIdsList()));
  }

  public ResponseEntity<?> getLobby(String lobbyUuid) {
    return fightRuntimeFacade
        .getLobby(lobbyUuid)
        .<ResponseEntity<?>>map(
            (lobby) ->
                ResponseEntity.ok(
                    Map.of(
                        "lobbyUuid", lobby.getLobbyUuid(),
                        "fightMode", lobby.getFightMode().name(),
                        "leader", lobby.getLeaderPlayerId(),
                        "players", lobby.getPlayerIdsList())))
        .orElseGet(
            () ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 2, "error", "Lobby doesn't exist")));
  }

  public ResponseEntity<?> joinLobby(String lobbyUuid, String player) {
    var result = fightRuntimeFacade.joinLobby(lobbyUuid, player);
    return switch (result) {
      case JOINED -> ResponseEntity.ok(Map.of("answer", "OK"));
      case ALREADY_IN_LOBBY -> ResponseEntity.ok(Map.of("answer", "ALREADY_IN_LOBBY"));
      case LOBBY_FULL ->
          ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("code", 8, "error", "Lobby is full"));
      case LOBBY_NOT_FOUND ->
          ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(Map.of("code", 2, "error", "Lobby doesn't exist"));
      case TRANSACTION_CONFLICT ->
          ResponseEntity.status(HttpStatus.CONFLICT)
              .body(Map.of("code", 9, "error", "Could not join lobby due to contention"));
    };
  }

  public ResponseEntity<?> leaveLobby(String lobbyUuid, String player) {
    var result = fightRuntimeFacade.leaveLobby(lobbyUuid, player);
    return switch (result) {
      case LEFT -> ResponseEntity.ok(Map.of("answer", "LEFT"));
      case LEFT_AND_LOBBY_CLOSED -> ResponseEntity.ok(Map.of("answer", "LEFT_AND_LOBBY_CLOSED"));
      case PLAYER_NOT_IN_LOBBY ->
          ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(Map.of("code", 8, "error", "Player is not in lobby"));
      case LOBBY_NOT_FOUND ->
          ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(Map.of("code", 2, "error", "Lobby doesn't exist"));
    };
  }

  public void closeLobby(String lobbyUuid) {
    fightRuntimeFacade.closeLobby(lobbyUuid);
  }

  public ResponseEntity<?> mapStartFightFailure(FightRuntimeFacade.StartFightResult result) {
    return switch (result.status()) {
      case LOBBY_NOT_FOUND ->
          ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(Map.of("code", 2, "error", "Lobby doesn't exist"));
      case INVALID_PLAYER_COUNT ->
          ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(Map.of("code", 8, "error", "Invalid number of players for the selected mode"));
      default ->
          ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", 9, "error", "Could not start fight"));
    };
  }

  public FightMode parseFightMode(String modeRaw) {
    if (modeRaw == null) {
      return FightMode.FIGHT_MODE_UNSPECIFIED;
    }
    var normalized = modeRaw.trim().toUpperCase();
    return switch (normalized) {
      case "PVP", "FIGHT_MODE_PVP" -> FightMode.FIGHT_MODE_PVP;
      case "SOLO_PVE", "SOLOPVE", "FIGHT_MODE_SOLO_PVE" -> FightMode.FIGHT_MODE_SOLO_PVE;
      case "TEAM_PVE", "TEAMPVE", "FIGHT_MODE_TEAM_PVE" -> FightMode.FIGHT_MODE_TEAM_PVE;
      default -> FightMode.FIGHT_MODE_UNSPECIFIED;
    };
  }
}
