package com.anf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.configuration.WebSocketsController;
import com.anf.service.fight.model.Fight;
import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Stats;
import com.anf.model.database.User;
import com.anf.infrastructure.state.FightRuntimeFacade;
import com.anf.infrastructure.state.FightRuntimeStore;
import com.anf.service.state.proto.GameStateModels.FightMode;
import com.anf.service.state.proto.GameStateModels.Lobby;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class FightStartServiceTest {
  private FightRuntimeFacade fightRuntimeFacade;
  private FightLobbyService fightLobbyService;
  private FightRuntimeFactoryService runtimeFactoryService;
  private FightRuntimeStore legacyFightRuntimeStore;
  private FightSnapshotService fightSnapshotService;
  private WebSocketsController webSocketsController;
  private FightStartService fightStartService;

  @BeforeEach
  void setUp() {
    fightRuntimeFacade = mock(FightRuntimeFacade.class);
    fightLobbyService = mock(FightLobbyService.class);
    runtimeFactoryService = mock(FightRuntimeFactoryService.class);
    legacyFightRuntimeStore = mock(FightRuntimeStore.class);
    fightSnapshotService = mock(FightSnapshotService.class);
    webSocketsController = mock(WebSocketsController.class);
    fightStartService =
        new FightStartService(
            fightRuntimeFacade,
            fightLobbyService,
            runtimeFactoryService,
            legacyFightRuntimeStore,
            fightSnapshotService,
            webSocketsController);
  }

  @Test
  void startFightFromLobby_returnsNotFound_whenLobbyDoesNotExist() {
    when(fightRuntimeFacade.getLobby("lobby-1")).thenReturn(Optional.empty());

    var response = fightStartService.startFightFromLobby("lobby-1", null, "alice", (fight, id) -> {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void startFightFromLobby_delegatesFailedStartMapping() {
    var lobby =
        Lobby.newBuilder()
            .setLobbyUuid("lobby-1")
            .setFightMode(FightMode.FIGHT_MODE_PVP)
            .addAllPlayerIds(List.of("alice", "bob"))
            .setLeaderPlayerId("alice")
            .build();
    when(fightRuntimeFacade.getLobby("lobby-1")).thenReturn(Optional.of(lobby));
    var failedStart =
        new FightRuntimeFacade.StartFightResult(
            FightRuntimeFacade.StartFightResultStatus.INVALID_PLAYER_COUNT, null, null);
    when(fightRuntimeFacade.startFightFromLobby("lobby-1")).thenReturn(failedStart);
    when(fightLobbyService.mapStartFightFailure(failedStart))
        .thenReturn(org.springframework.http.ResponseEntity.status(HttpStatus.BAD_REQUEST).build());

    var response = fightStartService.startFightFromLobby("lobby-1", null, "alice", (fight, id) -> {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void startFightFromLobby_startsPvpFightAndRunsFirstTurn() {
    var lobby =
        Lobby.newBuilder()
            .setLobbyUuid("lobby-1")
            .setFightMode(FightMode.FIGHT_MODE_PVP)
            .addAllPlayerIds(List.of("alice", "bob"))
            .setLeaderPlayerId("alice")
            .build();
    when(fightRuntimeFacade.getLobby("lobby-1")).thenReturn(Optional.of(lobby));
    var startedFight =
        com.anf.service.state.proto.GameStateModels.Fight.newBuilder()
            .setFightUuid("fight-1")
            .setFightMode(FightMode.FIGHT_MODE_PVP)
            .addAllParticipantUuids(List.of("alice", "bob"))
            .build();
    when(fightRuntimeFacade.startFightFromLobby("lobby-1"))
        .thenReturn(
            new FightRuntimeFacade.StartFightResult(
                FightRuntimeFacade.StartFightResultStatus.STARTED, startedFight, null));

    var runtimeFight = pvpFight("alice", "bob");
    when(runtimeFactoryService.createPvpRuntimeFight(List.of("alice", "bob"))).thenReturn(runtimeFight);

    var firstTurnCalled = new AtomicBoolean(false);
    var response =
        fightStartService.startFightFromLobby(
            "lobby-1", null, "alice", (fight, id) -> firstTurnCalled.set(true));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(firstTurnCalled.get()).isTrue();
    verify(legacyFightRuntimeStore).saveFight("fight-1", runtimeFight);
    verify(legacyFightRuntimeStore).markUserInFight("alice");
    verify(legacyFightRuntimeStore).markUserInFight("bob");
    verify(fightSnapshotService).syncFightSnapshot("fight-1", runtimeFight);
  }

  @Test
  void startFightFromLobby_returnsBadRequest_whenPveBossMissing() {
    var lobby =
        Lobby.newBuilder()
            .setLobbyUuid("lobby-1")
            .setFightMode(FightMode.FIGHT_MODE_TEAM_PVE)
            .addPlayerIds("alice")
            .setLeaderPlayerId("alice")
            .build();
    when(fightRuntimeFacade.getLobby("lobby-1")).thenReturn(Optional.of(lobby));
    var startedFight =
        com.anf.service.state.proto.GameStateModels.Fight.newBuilder()
            .setFightUuid("fight-1")
            .setFightMode(FightMode.FIGHT_MODE_TEAM_PVE)
            .addParticipantUuids("alice")
            .build();
    when(fightRuntimeFacade.startFightFromLobby("lobby-1"))
        .thenReturn(
            new FightRuntimeFacade.StartFightResult(
                FightRuntimeFacade.StartFightResultStatus.STARTED, startedFight, null));

    var response = fightStartService.startFightFromLobby("lobby-1", " ", "alice", (fight, id) -> {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    verify(runtimeFactoryService, never()).createPveRuntimeFight(any(), any());
  }

  private FightPVP pvpFight(String user1, String user2) {
    var fight = new FightPVP();
    fight.setFighters(user(user1).getCharacter(), user(user2).getCharacter());
    return fight;
  }

  private User user(String login) {
    var user = new User();
    user.setLogin(login);
    user.setStats(new Stats(120, 0, 0, 0, 0, 0, 1, 3));
    var character = new GameCharacter(0.1f, 100, 10, 30);
    character.setUser(user);
    user.setCharacter(character);
    return user;
  }
}
