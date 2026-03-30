package com.anf.service.state;

import com.anf.service.state.proto.GameStateModels.Fight;
import com.google.protobuf.InvalidProtocolBufferException;
import java.time.Duration;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RedisFightStore implements FightStore {
  private static final Duration FIGHT_TTL = Duration.ofHours(2);

  @Qualifier("protobufRedisTemplate")
  private final RedisTemplate<byte[], byte[]> redisTemplate;
  private final RedisCacheKeyFactory keyFactory;

  @Override
  public void createFight(Fight fight) {
    redisTemplate.opsForValue().set(keyFactory.fightKey(fight.getFightUuid()), fight.toByteArray(), FIGHT_TTL);
  }

  @Override
  public Optional<Fight> getFight(String fightUuid) {
    var payload = redisTemplate.opsForValue().get(keyFactory.fightKey(fightUuid));
    if (payload == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(Fight.parseFrom(payload));
    } catch (InvalidProtocolBufferException ex) {
      throw new IllegalStateException("Invalid fight payload", ex);
    }
  }

  @Override
  public void deleteFight(String fightUuid) {
    redisTemplate.delete(keyFactory.fightKey(fightUuid));
  }
}
