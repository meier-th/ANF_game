package com.anf.infrastructure.state;

import com.anf.service.state.proto.GameStateModels.FightMode;
import com.anf.service.state.proto.GameStateModels.CacheKey;
import com.anf.service.state.proto.GameStateModels.Lobby;
import com.google.protobuf.InvalidProtocolBufferException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RedisLobbyStore implements LobbyStore {
  private static final int PVP_CAPACITY = 2;
  private static final int SOLO_PVE_CAPACITY = 1;
  private static final int TEAM_PVE_CAPACITY = 4;
  private static final Duration LOBBY_TTL = Duration.ofMinutes(30);

  @Qualifier("protobufRedisTemplate")
  private final RedisTemplate<byte[], byte[]> redisTemplate;
  private final RedisCacheKeyFactory keyFactory;

  @Override
  public void createLobby(Lobby lobby) {
    redisTemplate.opsForValue().set(keyFactory.lobbyKey(lobby.getLobbyUuid()), lobby.toByteArray(), LOBBY_TTL);
  }

  @Override
  public Optional<Lobby> getLobby(String lobbyUuid) {
    var payload = redisTemplate.opsForValue().get(keyFactory.lobbyKey(lobbyUuid));
    if (payload == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(Lobby.parseFrom(payload));
    } catch (InvalidProtocolBufferException ex) {
      throw new IllegalStateException("Invalid lobby payload", ex);
    }
  }

  @Override
  public List<Lobby> listLobbies(FightMode mode) {
    return redisTemplate.execute(
        (RedisCallback<List<Lobby>>)
            (connection) -> {
              var lobbies = new ArrayList<Lobby>();
              try (var cursor = connection.scan(ScanOptions.scanOptions().count(1000).build())) {
                while (cursor.hasNext()) {
                  var key = cursor.next();
                  CacheKey cacheKey;
                  try {
                    cacheKey = CacheKey.parseFrom(key);
                  } catch (InvalidProtocolBufferException ex) {
                    continue;
                  }
                  if (cacheKey.getDataset() != CacheKey.Dataset.DATASET_LOBBY) {
                    continue;
                  }
                  var payload = connection.get(key);
                  if (payload == null) {
                    continue;
                  }
                  Lobby lobby;
                  try {
                    lobby = Lobby.parseFrom(payload);
                  } catch (InvalidProtocolBufferException ex) {
                    continue;
                  }
                  if (mode == null || mode == FightMode.FIGHT_MODE_UNSPECIFIED || lobby.getFightMode() == mode) {
                    lobbies.add(lobby);
                  }
                }
              }
              return lobbies;
            });
  }

  @Override
  public LobbyJoinResult joinLobby(String lobbyUuid, String playerId) {
    var key = keyFactory.lobbyKey(lobbyUuid);
    for (var attempt = 0; attempt < MAX_TRANSACTION_RETRIES; attempt++) {
      var result =
          redisTemplate.execute(
              new SessionCallback<LobbyJoinResult>() {
                @Override
                @SuppressWarnings("unchecked")
                public LobbyJoinResult execute(RedisOperations operations)
                    throws DataAccessException {
                  RedisOperations<byte[], byte[]> ops = operations;
                  ops.watch(key);
                  var payload = ops.opsForValue().get(key);
                  if (payload == null) {
                    ops.unwatch();
                    return LobbyJoinResult.LOBBY_NOT_FOUND;
                  }

                  Lobby currentLobby;
                  try {
                    currentLobby = Lobby.parseFrom(payload);
                  } catch (InvalidProtocolBufferException ex) {
                    ops.unwatch();
                    throw new IllegalStateException("Invalid lobby payload", ex);
                  }

                  if (currentLobby.getPlayerIdsList().contains(playerId)) {
                    ops.unwatch();
                    return LobbyJoinResult.ALREADY_IN_LOBBY;
                  }
                  if (currentLobby.getPlayerIdsCount() >= modeCapacity(currentLobby.getFightMode())) {
                    ops.unwatch();
                    return LobbyJoinResult.LOBBY_FULL;
                  }

                  var updatedLobby = currentLobby.toBuilder().addPlayerIds(playerId).build();
                  ops.multi();
                  ops.opsForValue().set(key, updatedLobby.toByteArray());
                  ops.expire(key, LOBBY_TTL);
                  var execResult = ops.exec();
                  return execResult == null ? LobbyJoinResult.TRANSACTION_CONFLICT : LobbyJoinResult.JOINED;
                }
              });
      if (result != LobbyJoinResult.TRANSACTION_CONFLICT) {
        return result;
      }
    }
    return LobbyJoinResult.TRANSACTION_CONFLICT;
  }

  @Override
  public LobbyLeaveResult leaveLobby(String lobbyUuid, String playerId) {
    var key = keyFactory.lobbyKey(lobbyUuid);
    var payload = redisTemplate.opsForValue().get(key);
    if (payload == null) {
      return LobbyLeaveResult.LOBBY_NOT_FOUND;
    }

    Lobby currentLobby;
    try {
      currentLobby = Lobby.parseFrom(payload);
    } catch (InvalidProtocolBufferException ex) {
      throw new IllegalStateException("Invalid lobby payload", ex);
    }

    if (!currentLobby.getPlayerIdsList().contains(playerId)) {
      return LobbyLeaveResult.PLAYER_NOT_IN_LOBBY;
    }

    var remainingPlayers =
        currentLobby.getPlayerIdsList().stream().filter((candidate) -> !candidate.equals(playerId)).toList();

    if (remainingPlayers.isEmpty()) {
      redisTemplate.delete(key);
      return LobbyLeaveResult.LEFT_AND_LOBBY_CLOSED;
    }

    var builder = currentLobby.toBuilder().clearPlayerIds().addAllPlayerIds(remainingPlayers);
    if (currentLobby.getLeaderPlayerId().equals(playerId)) {
      builder.setLeaderPlayerId(remainingPlayers.getFirst());
    }
    redisTemplate.opsForValue().set(key, builder.build().toByteArray(), LOBBY_TTL);
    return LobbyLeaveResult.LEFT;
  }

  @Override
  public void deleteLobby(String lobbyUuid) {
    redisTemplate.delete(keyFactory.lobbyKey(lobbyUuid));
  }

  private int modeCapacity(FightMode mode) {
    return switch (mode) {
      case FIGHT_MODE_PVP -> PVP_CAPACITY;
      case FIGHT_MODE_SOLO_PVE -> SOLO_PVE_CAPACITY;
      case FIGHT_MODE_TEAM_PVE -> TEAM_PVE_CAPACITY;
      default -> 0;
    };
  }
}
