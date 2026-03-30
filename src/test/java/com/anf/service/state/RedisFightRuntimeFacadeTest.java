package com.anf.service.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.service.state.proto.GameStateModels.FightMode;
import com.anf.service.state.proto.GameStateModels.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RedisFightRuntimeFacadeTest {
  private LobbyStore lobbyStore;
  private FightStore fightStore;
  private FightStateStore fightStateStore;
  private RedisFightRuntimeFacade runtimeFacade;

  @BeforeEach
  void setUp() {
    lobbyStore = Mockito.mock(LobbyStore.class);
    fightStore = Mockito.mock(FightStore.class);
    fightStateStore = Mockito.mock(FightStateStore.class);
    runtimeFacade = new RedisFightRuntimeFacade(lobbyStore, fightStore, fightStateStore);
  }

  @Test
  void createLobby_createsLobbyWithLeaderAsFirstPlayer() {
    var lobby = runtimeFacade.createLobby(FightMode.FIGHT_MODE_PVP, "leader");

    assertThat(lobby.getLobbyUuid()).isNotBlank();
    assertThat(lobby.getFightMode()).isEqualTo(FightMode.FIGHT_MODE_PVP);
    assertThat(lobby.getLeaderPlayerId()).isEqualTo("leader");
    assertThat(lobby.getPlayerIdsList()).containsExactly("leader");
    verify(lobbyStore).createLobby(lobby);
  }

  @Test
  void startFightFromLobby_returnsInvalidPlayerCount_whenPvpLobbyHasNotEnoughPlayers() {
    var lobby =
        Lobby.newBuilder()
            .setLobbyUuid("lobby-1")
            .setFightMode(FightMode.FIGHT_MODE_PVP)
            .addPlayerIds("p1")
            .setLeaderPlayerId("p1")
            .build();
    when(lobbyStore.getLobby("lobby-1")).thenReturn(java.util.Optional.of(lobby));

    var result = runtimeFacade.startFightFromLobby("lobby-1");

    assertThat(result.status()).isEqualTo(FightRuntimeFacade.StartFightResultStatus.INVALID_PLAYER_COUNT);
    assertThat(result.fight()).isNull();
    assertThat(result.fightState()).isNull();
  }

  @Test
  void startFightFromLobby_createsFightAndState_andClosesLobby() {
    var lobby =
        Lobby.newBuilder()
            .setLobbyUuid("lobby-1")
            .setFightMode(FightMode.FIGHT_MODE_TEAM_PVE)
            .addAllPlayerIds(java.util.List.of("p1", "p2", "p3"))
            .setLeaderPlayerId("p1")
            .build();
    when(lobbyStore.getLobby("lobby-1")).thenReturn(java.util.Optional.of(lobby));

    var result = runtimeFacade.startFightFromLobby("lobby-1");

    assertThat(result.status()).isEqualTo(FightRuntimeFacade.StartFightResultStatus.STARTED);
    assertThat(result.fight()).isNotNull();
    assertThat(result.fightState()).isNotNull();
    assertThat(result.fight().getParticipantUuidsList()).containsExactly("p1", "p2", "p3");
    assertThat(result.fightState().getCreatureStatusesMap()).containsKeys("p1", "p2", "p3");
    verify(fightStore).createFight(result.fight());
    verify(fightStateStore).createFightState(result.fightState());
    verify(lobbyStore).deleteLobby("lobby-1");
  }
}
