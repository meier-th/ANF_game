package com.anf.service.state;

import com.anf.service.state.proto.GameStateModels.FightIdAlias;
import com.google.protobuf.InvalidProtocolBufferException;
import java.time.Duration;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RedisFightIdAliasStore implements FightIdAliasStore {
  private static final Duration ID_ALIAS_TTL = Duration.ofHours(4);

  @Qualifier("protobufRedisTemplate")
  private final RedisTemplate<byte[], byte[]> redisTemplate;
  private final RedisCacheKeyFactory keyFactory;

  @Override
  public void saveMapping(int legacyFightId, String fightUuid) {
    var payload =
        FightIdAlias.newBuilder().setLegacyFightId(legacyFightId).setFightUuid(fightUuid).build().toByteArray();
    redisTemplate.opsForValue().set(keyFactory.fightIdAliasKey(legacyFightId), payload, ID_ALIAS_TTL);
  }

  @Override
  public Optional<String> getFightUuid(int legacyFightId) {
    var payload = redisTemplate.opsForValue().get(keyFactory.fightIdAliasKey(legacyFightId));
    if (payload == null) {
      return Optional.empty();
    }
    try {
      var alias = FightIdAlias.parseFrom(payload);
      return Optional.of(alias.getFightUuid());
    } catch (InvalidProtocolBufferException ex) {
      throw new IllegalStateException("Invalid fight-id alias payload", ex);
    }
  }

  @Override
  public void deleteMapping(int legacyFightId) {
    redisTemplate.delete(keyFactory.fightIdAliasKey(legacyFightId));
  }
}
