package com.anf.service.state;

import com.anf.service.state.proto.GameStateModels.CreatureStatus;
import com.anf.service.state.proto.GameStateModels.Fight;
import com.anf.service.state.proto.GameStateModels.FightMode;
import com.anf.service.state.proto.GameStateModels.FightState;
import com.anf.service.state.proto.GameStateModels.Lobby;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RedisFightRuntimeFacade implements FightRuntimeFacade {
  private static final int PVP_PLAYERS = 2;
  private static final int SOLO_PVE_PLAYERS = 1;
  private static final int TEAM_PVE_MIN_PLAYERS = 1;

  private final LobbyStore lobbyStore;
  private final FightStore fightStore;
  private final FightStateStore fightStateStore;

  @Override
  public Lobby createLobby(FightMode mode, String leaderPlayerId) {
    var lobby =
        Lobby.newBuilder()
            .setLobbyUuid(UUID.randomUUID().toString())
            .setFightMode(mode)
            .addPlayerIds(leaderPlayerId)
            .setLeaderPlayerId(leaderPlayerId)
            .build();
    lobbyStore.createLobby(lobby);
    return lobby;
  }

  @Override
  public Optional<Lobby> getLobby(String lobbyUuid) {
    return lobbyStore.getLobby(lobbyUuid);
  }

  @Override
  public LobbyStore.LobbyJoinResult joinLobby(String lobbyUuid, String playerId) {
    return lobbyStore.joinLobby(lobbyUuid, playerId);
  }

  @Override
  public LobbyStore.LobbyLeaveResult leaveLobby(String lobbyUuid, String playerId) {
    return lobbyStore.leaveLobby(lobbyUuid, playerId);
  }

  @Override
  public void closeLobby(String lobbyUuid) {
    lobbyStore.deleteLobby(lobbyUuid);
  }

  @Override
  public StartFightResult startFightFromLobby(String lobbyUuid) {
    var lobby = lobbyStore.getLobby(lobbyUuid);
    if (lobby.isEmpty()) {
      return new StartFightResult(StartFightResultStatus.LOBBY_NOT_FOUND, null, null);
    }

    var lobbyValue = lobby.get();
    if (!isValidPlayerCount(lobbyValue)) {
      return new StartFightResult(StartFightResultStatus.INVALID_PLAYER_COUNT, null, null);
    }

    var fightUuid = UUID.randomUUID().toString();
    var fight =
        Fight.newBuilder()
            .setFightUuid(fightUuid)
            .setFightMode(lobbyValue.getFightMode())
            .addAllParticipantUuids(lobbyValue.getPlayerIdsList())
            .build();
    var initialCreatureStatuses =
        lobbyValue.getPlayerIdsList().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    playerId -> playerId, playerId -> CreatureStatus.newBuilder().build()));
    var fightState =
        FightState.newBuilder()
            .setFightUuid(fightUuid)
            .putAllCreatureStatuses(initialCreatureStatuses)
            .build();

    fightStore.createFight(fight);
    fightStateStore.createFightState(fightState);
    lobbyStore.deleteLobby(lobbyUuid);
    return new StartFightResult(StartFightResultStatus.STARTED, fight, fightState);
  }

  private boolean isValidPlayerCount(Lobby lobby) {
    var players = lobby.getPlayerIdsCount();
    return switch (lobby.getFightMode()) {
      case FIGHT_MODE_PVP -> players == PVP_PLAYERS;
      case FIGHT_MODE_SOLO_PVE -> players == SOLO_PVE_PLAYERS;
      case FIGHT_MODE_TEAM_PVE -> players >= TEAM_PVE_MIN_PLAYERS;
      default -> false;
    };
  }
}
