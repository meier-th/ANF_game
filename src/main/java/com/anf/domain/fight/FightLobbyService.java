package com.anf.domain.fight;

import com.anf.domain.shared.ApiAnswer;
import com.anf.domain.shared.ApiField;
import com.anf.domain.shared.ApiMessage;
import com.anf.domain.shared.ErrorCode;
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
          .body(
              Map.of(
                  ApiField.CODE.getValue(),
                  ErrorCode.INVALID_REQUEST.getValue(),
                  ApiField.ERROR.getValue(),
                  ApiMessage.UNSUPPORTED_FIGHT_MODE.getValue()));
    }
    var lobby = fightRuntimeFacade.createLobby(parsedMode, leader);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            Map.of(
                ApiField.LOBBY_UUID.getValue(), lobby.getLobbyUuid(),
                ApiField.FIGHT_MODE.getValue(), lobby.getFightMode().name(),
                ApiField.LEADER.getValue(), lobby.getLeaderPlayerId(),
                ApiField.PLAYERS.getValue(), lobby.getPlayerIdsList()));
  }

  public ResponseEntity<?> getLobby(String lobbyUuid) {
    return fightRuntimeFacade
        .getLobby(lobbyUuid)
        .<ResponseEntity<?>>map(
            (lobby) ->
                ResponseEntity.ok(
                    Map.of(
                        ApiField.LOBBY_UUID.getValue(), lobby.getLobbyUuid(),
                        ApiField.FIGHT_MODE.getValue(), lobby.getFightMode().name(),
                        ApiField.LEADER.getValue(), lobby.getLeaderPlayerId(),
                        ApiField.PLAYERS.getValue(), lobby.getPlayerIdsList())))
        .orElseGet(
            () ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                        Map.of(
                            ApiField.CODE.getValue(),
                            ErrorCode.NOT_FOUND.getValue(),
                            ApiField.ERROR.getValue(),
                            ApiMessage.LOBBY_NOT_FOUND.getValue())));
  }

  public ResponseEntity<?> joinLobby(String lobbyUuid, String player) {
    var result = fightRuntimeFacade.joinLobby(lobbyUuid, player);
    return switch (result) {
      case JOINED ->
          ResponseEntity.ok(Map.of(ApiField.ANSWER.getValue(), ApiAnswer.OK.getValue()));
      case ALREADY_IN_LOBBY ->
          ResponseEntity.ok(Map.of(ApiField.ANSWER.getValue(), ApiAnswer.ALREADY_IN_LOBBY.getValue()));
      case LOBBY_FULL ->
          ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(
                  Map.of(
                      ApiField.CODE.getValue(),
                      ErrorCode.INVALID_REQUEST.getValue(),
                      ApiField.ERROR.getValue(),
                      ApiMessage.LOBBY_IS_FULL.getValue()));
      case LOBBY_NOT_FOUND ->
          ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(
                  Map.of(
                      ApiField.CODE.getValue(),
                      ErrorCode.NOT_FOUND.getValue(),
                      ApiField.ERROR.getValue(),
                      ApiMessage.LOBBY_NOT_FOUND.getValue()));
      case TRANSACTION_CONFLICT ->
          ResponseEntity.status(HttpStatus.CONFLICT)
              .body(
                  Map.of(
                      ApiField.CODE.getValue(),
                      ErrorCode.CONFLICT.getValue(),
                      ApiField.ERROR.getValue(),
                      ApiMessage.COULD_NOT_JOIN_LOBBY.getValue()));
    };
  }

  public ResponseEntity<?> leaveLobby(String lobbyUuid, String player) {
    var result = fightRuntimeFacade.leaveLobby(lobbyUuid, player);
    return switch (result) {
      case LEFT ->
          ResponseEntity.ok(Map.of(ApiField.ANSWER.getValue(), ApiAnswer.LEFT.getValue()));
      case LEFT_AND_LOBBY_CLOSED ->
          ResponseEntity.ok(
              Map.of(ApiField.ANSWER.getValue(), ApiAnswer.LEFT_AND_LOBBY_CLOSED.getValue()));
      case PLAYER_NOT_IN_LOBBY ->
          ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(
                  Map.of(
                      ApiField.CODE.getValue(),
                      ErrorCode.INVALID_REQUEST.getValue(),
                      ApiField.ERROR.getValue(),
                      ApiMessage.PLAYER_NOT_IN_LOBBY.getValue()));
      case LOBBY_NOT_FOUND ->
          ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(
                  Map.of(
                      ApiField.CODE.getValue(),
                      ErrorCode.NOT_FOUND.getValue(),
                      ApiField.ERROR.getValue(),
                      ApiMessage.LOBBY_NOT_FOUND.getValue()));
    };
  }

  public void closeLobby(String lobbyUuid) {
    fightRuntimeFacade.closeLobby(lobbyUuid);
  }

  public ResponseEntity<?> mapStartFightFailure(FightRuntimeFacade.StartFightResult result) {
    return switch (result.status()) {
      case LOBBY_NOT_FOUND ->
          ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(
                  Map.of(
                      ApiField.CODE.getValue(),
                      ErrorCode.NOT_FOUND.getValue(),
                      ApiField.ERROR.getValue(),
                      ApiMessage.LOBBY_NOT_FOUND.getValue()));
      case INVALID_PLAYER_COUNT ->
          ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(
                  Map.of(
                      ApiField.CODE.getValue(),
                      ErrorCode.INVALID_REQUEST.getValue(),
                      ApiField.ERROR.getValue(),
                      ApiMessage.INVALID_PLAYER_COUNT.getValue()));
      default ->
          ResponseEntity.status(HttpStatus.CONFLICT)
              .body(
                  Map.of(
                      ApiField.CODE.getValue(),
                      ErrorCode.CONFLICT.getValue(),
                      ApiField.ERROR.getValue(),
                      ApiMessage.COULD_NOT_START_FIGHT.getValue()));
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
