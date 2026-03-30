package com.anf.service.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisRuntimeMetadataStoreTest {
  private RedisTemplate<byte[], byte[]> redisTemplate;
  private ValueOperations<byte[], byte[]> valueOperations;
  private RedisCacheKeyFactory keyFactory;
  private RedisFightIdAliasStore aliasStore;

  @BeforeEach
  void setUp() {
    redisTemplate = mock(RedisTemplate.class);
    valueOperations = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    keyFactory = new RedisCacheKeyFactory();
    aliasStore = new RedisFightIdAliasStore(redisTemplate, keyFactory);
  }

  @Test
  void alias_roundTripWithProtobufSerialization() {
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

    aliasStore.saveMapping(42, "fight-uuid-1");

    var alias = aliasStore.getFightUuid(42);

    assertThat(alias).contains("fight-uuid-1");
    assertThat(redisData).containsKey(Base64.getEncoder().encodeToString(keyFactory.fightIdAliasKey(42)));
  }

  @Test
  void delete_removeDataByKey() {
    aliasStore.deleteMapping(42);

    verify(redisTemplate).delete(keyFactory.fightIdAliasKey(42));
  }
}
