package com.anf.service.state;

import com.anf.service.state.proto.GameStateModels.FightState;
import com.google.protobuf.InvalidProtocolBufferException;
import java.time.Duration;
import java.util.Optional;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RedisFightStateStore implements FightStateStore {
  private static final Duration FIGHT_STATE_TTL = Duration.ofHours(4);

  @Qualifier("protobufRedisTemplate")
  private final RedisTemplate<byte[], byte[]> redisTemplate;
  private final RedisCacheKeyFactory keyFactory;

  @Override
  public void createFightState(FightState fightState) {
    redisTemplate
        .opsForValue()
        .set(keyFactory.fightStateKey(fightState.getFightUuid()), fightState.toByteArray(), FIGHT_STATE_TTL);
  }

  @Override
  public Optional<FightState> getFightState(String fightUuid) {
    return readEntity(keyFactory.fightStateKey(fightUuid), FightState::parseFrom);
  }

  @Override
  public FightStateUpdateResult updateFightState(
      String fightUuid, UnaryOperator<FightState> updater) {
    var key = keyFactory.fightStateKey(fightUuid);
    for (var attempt = 0; attempt < MAX_TRANSACTION_RETRIES; attempt++) {
      var result =
          redisTemplate.execute(
              new SessionCallback<FightStateUpdateResult>() {
                @Override
                @SuppressWarnings("unchecked")
                public FightStateUpdateResult execute(RedisOperations operations)
                    throws DataAccessException {
                  RedisOperations<byte[], byte[]> ops = operations;
                  ops.watch(key);
                  var payload = ops.opsForValue().get(key);
                  if (payload == null) {
                    ops.unwatch();
                    return FightStateUpdateResult.FIGHT_STATE_NOT_FOUND;
                  }

                  FightState currentState;
                  try {
                    currentState = FightState.parseFrom(payload);
                  } catch (InvalidProtocolBufferException ex) {
                    ops.unwatch();
                    throw new IllegalStateException("Invalid fight state payload", ex);
                  }

                  var updatedState = updater.apply(currentState);
                  if (updatedState == null) {
                    ops.unwatch();
                    throw new IllegalArgumentException("Fight state updater returned null.");
                  }

                  ops.multi();
                  ops.opsForValue().set(key, updatedState.toByteArray());
                  ops.expire(key, FIGHT_STATE_TTL);
                  var execResult = ops.exec();
                  return execResult == null
                      ? FightStateUpdateResult.TRANSACTION_CONFLICT
                      : FightStateUpdateResult.UPDATED;
                }
              });

      if (result != FightStateUpdateResult.TRANSACTION_CONFLICT) {
        return result;
      }
    }
    return FightStateUpdateResult.TRANSACTION_CONFLICT;
  }

  private <T> Optional<T> readEntity(byte[] key, ProtobufReader<T> reader) {
    var payload = redisTemplate.opsForValue().get(key);
    if (payload == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(reader.read(payload));
    } catch (InvalidProtocolBufferException ex) {
      throw new IllegalStateException("Invalid protobuf payload", ex);
    }
  }

  @FunctionalInterface
  private interface ProtobufReader<T> {
    T read(byte[] payload) throws InvalidProtocolBufferException;
  }
}
