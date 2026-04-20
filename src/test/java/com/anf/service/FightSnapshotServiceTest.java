package com.anf.service;

import com.anf.domain.auth.*;
import com.anf.domain.combat.*;
import com.anf.domain.fight.*;
import com.anf.domain.social.*;
import com.anf.domain.user.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.model.database.FightPVP;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Stats;
import com.anf.model.database.User;
import com.anf.infrastructure.state.FightStateStore;
import com.anf.infrastructure.state.FightStore;
import com.anf.service.state.proto.GameStateModels.Fight;
import com.anf.service.state.proto.GameStateModels.FightState;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FightSnapshotServiceTest {
  private FightStore fightStore;
  private FightStateStore fightStateStore;
  private FightSnapshotService fightSnapshotService;

  @BeforeEach
  void setUp() {
    fightStore = mock(FightStore.class);
    fightStateStore = mock(FightStateStore.class);
    fightSnapshotService = new FightSnapshotService(fightStore, fightStateStore);
  }

  @Test
  void hasProtobufState_returnsTrueOnlyWhenBothRecordsExist() {
    when(fightStore.getFight("fight-1")).thenReturn(Optional.of(Fight.getDefaultInstance()));
    when(fightStateStore.getFightState("fight-1")).thenReturn(Optional.of(FightState.getDefaultInstance()));
    assertThat(fightSnapshotService.hasProtobufState("fight-1")).isTrue();

    when(fightStateStore.getFightState("fight-1")).thenReturn(Optional.empty());
    assertThat(fightSnapshotService.hasProtobufState("fight-1")).isFalse();
  }

  @Test
  void syncFightSnapshot_updatesCreatureStatusesAndTurnHistory() {
    var updatedState = new AtomicReference<FightState>();
    doAnswer(
            (invocation) -> {
              var updater = invocation.getArgument(1, java.util.function.UnaryOperator.class);
              var baseState = FightState.newBuilder().setFightUuid("fight-1").build();
              updatedState.set((FightState) updater.apply(baseState));
              return FightStateStore.FightStateUpdateResult.UPDATED;
            })
        .when(fightStateStore)
        .updateFightState(eq("fight-1"), any());

    var fight = new FightPVP();
    var alice = userWithCharacter("alice", 90);
    var bob = userWithCharacter("bob", 70);
    fight.setFighters(alice.getCharacter(), bob.getCharacter());
    fight.switchAttacker();

    fightSnapshotService.syncFightSnapshot("fight-1", fight);

    assertThat(updatedState.get()).isNotNull();
    assertThat(updatedState.get().getCreatureStatusesMap()).containsKeys("alice", "bob");
    assertThat(updatedState.get().getTakenTurnsCount()).isEqualTo(1);
    assertThat(updatedState.get().getTakenTurns(0).getCharacterUuid()).isEqualTo("alice");
  }

  @Test
  void deleteFightArtifacts_cleansBothProtobufRecords_andLegacyState() {
    var legacyDeleted = new AtomicBoolean(false);

    fightSnapshotService.deleteFightArtifacts("fight-1", () -> legacyDeleted.set(true));

    assertThat(legacyDeleted.get()).isTrue();
    verify(fightStore).deleteFight("fight-1");
    verify(fightStateStore).deleteFightState("fight-1");
  }

  private User userWithCharacter(String login, int hp) {
    var user = new User();
    user.setLogin(login);
    user.setStats(new Stats(100, 0, 0, 0, 0, 0, 1, 3));
    var character = new GameCharacter(0.1f, 100, 10, 30);
    character.setUser(user);
    character.setCurrentHP(hp);
    user.setCharacter(character);
    return user;
  }
}
