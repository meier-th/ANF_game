package com.anf.service.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.service.state.FightStateStore.FightStateUpdateResult;
import com.anf.service.state.LobbyStore.LobbyJoinResult;
import com.anf.service.state.LobbyStore.LobbyLeaveResult;
import com.anf.service.state.proto.GameStateModels.Fight;
import com.anf.service.state.proto.GameStateModels.FightMode;
import com.anf.service.state.proto.GameStateModels.FightState;
import com.anf.service.state.proto.GameStateModels.Lobby;
import com.anf.service.state.proto.GameStateModels.TakenTurn;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;

class RedisFightStateStoreTest {
  private RedisTemplate<byte[], byte[]> redisTemplate;
  private ValueOperations<byte[], byte[]> valueOperations;
  private RedisCacheKeyFactory keyFactory;
  private RedisLobbyStore lobbyStore;
  private RedisFightStore fightStore;
  private RedisFightStateStore fightStateStore;

  @BeforeEach
  void setUp() {
    redisTemplate = mock(RedisTemplate.class);
    valueOperations = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    keyFactory = new RedisCacheKeyFactory();
    lobbyStore = new RedisLobbyStore(redisTemplate, keyFactory);
    fightStore = new RedisFightStore(redisTemplate, keyFactory);
    fightStateStore = new RedisFightStateStore(redisTemplate, keyFactory);
  }

  @Test
  void joinLobby_returnsLobbyFull_whenCapacityReached() {
    var fullLobby =
        Lobby.newBuilder()
            .setLobbyUuid("lobby-1")
            .setFightMode(FightMode.FIGHT_MODE_PVP)
            .addAllPlayerIds(List.of("p1", "p2"))
            .setLeaderPlayerId("p1")
            .build();

    when(redisTemplate.execute(any(SessionCallback.class)))
        .thenAnswer(
            (Answer<LobbyJoinResult>)
                invocation -> {
                  SessionCallback<LobbyJoinResult> callback = invocation.getArgument(0);
                  RedisOperations<byte[], byte[]> operations = mock(RedisOperations.class);
                  ValueOperations<byte[], byte[]> ops = mock(ValueOperations.class);
                  when(operations.opsForValue()).thenReturn(ops);
                  when(ops.get(any())).thenReturn(fullLobby.toByteArray());
                  return callback.execute(operations);
                });

    var result = lobbyStore.joinLobby("lobby-1", "p3");

    assertThat(result).isEqualTo(LobbyJoinResult.LOBBY_FULL);
  }

  @Test
  void joinLobby_retriesAndSucceeds_afterConflict() {
    when(redisTemplate.execute(any(SessionCallback.class)))
        .thenReturn(LobbyJoinResult.TRANSACTION_CONFLICT, LobbyJoinResult.JOINED);

    var result = lobbyStore.joinLobby("lobby-1", "p2");

    assertThat(result).isEqualTo(LobbyJoinResult.JOINED);
    verify(redisTemplate, times(2)).execute(any(SessionCallback.class));
  }

  @Test
  void leaveLobby_closesLobby_whenLeaderLeavesAndNoPlayersRemain_withoutTransaction() {
    var singlePlayerLobby =
        Lobby.newBuilder()
            .setLobbyUuid("lobby-1")
            .setFightMode(FightMode.FIGHT_MODE_SOLO_PVE)
            .addPlayerIds("leader")
            .setLeaderPlayerId("leader")
            .build();

    when(valueOperations.get(any())).thenReturn(singlePlayerLobby.toByteArray());

    var result = lobbyStore.leaveLobby("lobby-1", "leader");

    assertThat(result).isEqualTo(LobbyLeaveResult.LEFT_AND_LOBBY_CLOSED);
    verify(redisTemplate, times(1)).delete(any(byte[].class));
    verify(redisTemplate, times(0)).execute(any(SessionCallback.class));
  }

  @Test
  void fightAndFightState_roundTripWithProtobufSerialization() {
    var redisData = new HashMap<String, byte[]>();
    when(valueOperations.get(any()))
        .thenAnswer(
            (invocation) ->
                redisData.get(
                    Base64.getEncoder().encodeToString(invocation.getArgument(0, byte[].class))));
    doAnswer(
            invocation -> {
              byte[] key = invocation.getArgument(0);
              byte[] payload = invocation.getArgument(1);
              redisData.put(Base64.getEncoder().encodeToString(key), payload);
              return null;
            })
        .when(valueOperations)
        .set(any(), any(), any(Duration.class));

    var fight =
        Fight.newBuilder()
            .setFightUuid("fight-1")
            .setFightMode(FightMode.FIGHT_MODE_TEAM_PVE)
            .addAllParticipantUuids(List.of("c1", "c2", "c3"))
            .build();
    var fightState =
        FightState.newBuilder()
            .setFightUuid("fight-1")
            .addTakenTurns(TakenTurn.newBuilder().setCharacterUuid("c1").setTimestamp(100).build())
            .build();

    fightStore.createFight(fight);
    fightStateStore.createFightState(fightState);

    assertThat(fightStore.getFight("fight-1")).contains(fight);
    assertThat(fightStateStore.getFightState("fight-1")).contains(fightState);
    assertThat(redisData)
        .containsKeys(
            Base64.getEncoder().encodeToString(keyFactory.fightKey("fight-1")),
            Base64.getEncoder().encodeToString(keyFactory.fightStateKey("fight-1")));
  }

  @Test
  void updateFightState_returnsConflict_afterMaxRetries() {
    when(redisTemplate.execute(any(SessionCallback.class)))
        .thenReturn(
            FightStateUpdateResult.TRANSACTION_CONFLICT,
            FightStateUpdateResult.TRANSACTION_CONFLICT,
            FightStateUpdateResult.TRANSACTION_CONFLICT,
            FightStateUpdateResult.TRANSACTION_CONFLICT,
            FightStateUpdateResult.TRANSACTION_CONFLICT);

    var result = fightStateStore.updateFightState("fight-1", (state) -> state);

    assertThat(result).isEqualTo(FightStateUpdateResult.TRANSACTION_CONFLICT);
    verify(redisTemplate, times(FightStateStore.MAX_TRANSACTION_RETRIES))
        .execute(any(SessionCallback.class));
  }
}
