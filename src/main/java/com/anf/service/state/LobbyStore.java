package com.anf.service.state;

import com.anf.service.state.proto.GameStateModels.Lobby;
import java.util.Optional;

public interface LobbyStore {
  int MAX_TRANSACTION_RETRIES = 5;

  enum LobbyJoinResult {
    JOINED,
    LOBBY_NOT_FOUND,
    ALREADY_IN_LOBBY,
    LOBBY_FULL,
    TRANSACTION_CONFLICT
  }

  enum LobbyLeaveResult {
    LEFT,
    LEFT_AND_LOBBY_CLOSED,
    LOBBY_NOT_FOUND,
    PLAYER_NOT_IN_LOBBY
  }

  void createLobby(Lobby lobby);

  Optional<Lobby> getLobby(String lobbyUuid);

  LobbyJoinResult joinLobby(String lobbyUuid, String playerId);

  LobbyLeaveResult leaveLobby(String lobbyUuid, String playerId);

  void deleteLobby(String lobbyUuid);
}
