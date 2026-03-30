package com.anf.service.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.anf.model.database.FightPVP;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

class RedisLegacyFightRuntimeStoreTest {
  private RedisTemplate<String, Object> redisTemplate;
  private ValueOperations<String, Object> valueOperations;
  private SetOperations<String, Object> setOperations;
  private RedisLegacyFightRuntimeStore runtimeStore;

  @BeforeEach
  void setUp() {
    redisTemplate = mock(RedisTemplate.class);
    valueOperations = mock(ValueOperations.class);
    setOperations = mock(SetOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(redisTemplate.opsForSet()).thenReturn(setOperations);
    runtimeStore = new RedisLegacyFightRuntimeStore(redisTemplate);
  }

  @Test
  void saveAndGetFight_roundTripViaRedisValueStore() {
    var fight = new FightPVP();
    var fightUuid = "fight-uuid-1";
    var key = "anf:runtime:fight:" + fightUuid;
    when(valueOperations.get(key)).thenReturn(fight);

    runtimeStore.saveFight(fightUuid, fight);
    var loaded = runtimeStore.getFight(fightUuid);

    assertThat(loaded).contains(fight);
  }

  @Test
  void isUserInFight_delegatesToRedisSetMembership() {
    when(setOperations.isMember("anf:runtime:users-in-fight", "alice")).thenReturn(true);

    var result = runtimeStore.isUserInFight("alice");

    assertThat(result).isTrue();
  }
}
