package com.anf.service.state;

import com.anf.service.state.proto.GameStateModels.Fight;
import com.anf.service.state.proto.GameStateModels.FightMode;
import com.anf.service.state.proto.GameStateModels.FightState;
import com.anf.service.state.proto.GameStateModels.Lobby;
import java.util.Optional;

public interface FightRuntimeFacade {

  enum StartFightResultStatus {
    STARTED,
    LOBBY_NOT_FOUND,
    INVALID_PLAYER_COUNT
  }

  record StartFightResult(StartFightResultStatus status, Fight fight, FightState fightState) {}

  Lobby createLobby(FightMode mode, String leaderPlayerId);

  Optional<Lobby> getLobby(String lobbyUuid);

  LobbyStore.LobbyJoinResult joinLobby(String lobbyUuid, String playerId);

  LobbyStore.LobbyLeaveResult leaveLobby(String lobbyUuid, String playerId);

  void closeLobby(String lobbyUuid);

  StartFightResult startFightFromLobby(String lobbyUuid);
}
