package com.anf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.service.state.FightRuntimeFacade;
import com.anf.service.state.LobbyStore;
import com.anf.service.state.proto.GameStateModels.FightMode;
import com.anf.service.state.proto.GameStateModels.Lobby;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class FightLobbyServiceTest {
  private FightRuntimeFacade fightRuntimeFacade;
  private FightLobbyService fightLobbyService;

  @BeforeEach
  void setUp() {
    fightRuntimeFacade = mock(FightRuntimeFacade.class);
    fightLobbyService = new FightLobbyService(fightRuntimeFacade);
  }

  @Test
  void createLobby_returnsBadRequest_forUnsupportedMode() {
    var response = fightLobbyService.createLobby("unsupported", "alice");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isEqualTo(java.util.Map.of("code", 8, "error", "Unsupported fight mode"));
  }

  @Test
  void createLobby_returnsCreated_whenModeIsValid() {
    var lobby =
        Lobby.newBuilder()
            .setLobbyUuid("lobby-1")
            .setFightMode(FightMode.FIGHT_MODE_PVP)
            .setLeaderPlayerId("alice")
            .addPlayerIds("alice")
            .build();
    when(fightRuntimeFacade.createLobby(FightMode.FIGHT_MODE_PVP, "alice")).thenReturn(lobby);

    var response = fightLobbyService.createLobby("pvp", "alice");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody())
        .isEqualTo(
            java.util.Map.of(
                "lobbyUuid", "lobby-1",
                "fightMode", "FIGHT_MODE_PVP",
                "leader", "alice",
                "players", java.util.List.of("alice")));
  }

  @Test
  void getLobby_returnsNotFound_whenLobbyIsMissing() {
    when(fightRuntimeFacade.getLobby("lobby-1")).thenReturn(Optional.empty());

    var response = fightLobbyService.getLobby("lobby-1");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isEqualTo(java.util.Map.of("code", 2, "error", "Lobby doesn't exist"));
  }

  @Test
  void joinLobby_mapsAllJoinResults() {
    when(fightRuntimeFacade.joinLobby("lobby-1", "alice")).thenReturn(LobbyStore.LobbyJoinResult.JOINED);
    assertThat(fightLobbyService.joinLobby("lobby-1", "alice").getStatusCode()).isEqualTo(HttpStatus.OK);

    when(fightRuntimeFacade.joinLobby("lobby-1", "alice"))
        .thenReturn(LobbyStore.LobbyJoinResult.ALREADY_IN_LOBBY);
    assertThat(fightLobbyService.joinLobby("lobby-1", "alice").getBody())
        .isEqualTo(java.util.Map.of("answer", "ALREADY_IN_LOBBY"));

    when(fightRuntimeFacade.joinLobby("lobby-1", "alice")).thenReturn(LobbyStore.LobbyJoinResult.LOBBY_FULL);
    assertThat(fightLobbyService.joinLobby("lobby-1", "alice").getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);

    when(fightRuntimeFacade.joinLobby("lobby-1", "alice"))
        .thenReturn(LobbyStore.LobbyJoinResult.LOBBY_NOT_FOUND);
    assertThat(fightLobbyService.joinLobby("lobby-1", "alice").getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);

    when(fightRuntimeFacade.joinLobby("lobby-1", "alice"))
        .thenReturn(LobbyStore.LobbyJoinResult.TRANSACTION_CONFLICT);
    assertThat(fightLobbyService.joinLobby("lobby-1", "alice").getStatusCode())
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void leaveLobby_mapsAllLeaveResults() {
    when(fightRuntimeFacade.leaveLobby("lobby-1", "alice")).thenReturn(LobbyStore.LobbyLeaveResult.LEFT);
    assertThat(fightLobbyService.leaveLobby("lobby-1", "alice").getBody())
        .isEqualTo(java.util.Map.of("answer", "LEFT"));

    when(fightRuntimeFacade.leaveLobby("lobby-1", "alice"))
        .thenReturn(LobbyStore.LobbyLeaveResult.LEFT_AND_LOBBY_CLOSED);
    assertThat(fightLobbyService.leaveLobby("lobby-1", "alice").getBody())
        .isEqualTo(java.util.Map.of("answer", "LEFT_AND_LOBBY_CLOSED"));

    when(fightRuntimeFacade.leaveLobby("lobby-1", "alice"))
        .thenReturn(LobbyStore.LobbyLeaveResult.PLAYER_NOT_IN_LOBBY);
    assertThat(fightLobbyService.leaveLobby("lobby-1", "alice").getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);

    when(fightRuntimeFacade.leaveLobby("lobby-1", "alice"))
        .thenReturn(LobbyStore.LobbyLeaveResult.LOBBY_NOT_FOUND);
    assertThat(fightLobbyService.leaveLobby("lobby-1", "alice").getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void mapStartFightFailure_mapsKnownStatuses() {
    var notFoundResult =
        new FightRuntimeFacade.StartFightResult(
            FightRuntimeFacade.StartFightResultStatus.LOBBY_NOT_FOUND, null, null);
    assertThat(fightLobbyService.mapStartFightFailure(notFoundResult).getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);

    var invalidCountResult =
        new FightRuntimeFacade.StartFightResult(
            FightRuntimeFacade.StartFightResultStatus.INVALID_PLAYER_COUNT, null, null);
    assertThat(fightLobbyService.mapStartFightFailure(invalidCountResult).getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void closeLobby_delegatesToRuntimeFacade() {
    fightLobbyService.closeLobby("lobby-1");
    verify(fightRuntimeFacade).closeLobby("lobby-1");
  }
}
