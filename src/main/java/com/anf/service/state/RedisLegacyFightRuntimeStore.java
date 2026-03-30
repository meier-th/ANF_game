package com.anf.service.state;

import com.anf.model.Fight;
import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Primary
@AllArgsConstructor
public class RedisLegacyFightRuntimeStore implements LegacyFightRuntimeStore {
  private static final String USERS_IN_FIGHT_KEY = "anf:runtime:users-in-fight";
  private static final Duration FIGHT_TTL = Duration.ofHours(4);

  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public Optional<Fight> getFight(String fightUuid) {
    var value = redisTemplate.opsForValue().get(fightKey(fightUuid));
    if (value == null) {
      return Optional.empty();
    }
    return Optional.of((Fight) value);
  }

  @Override
  public void saveFight(String fightUuid, Fight fight) {
    redisTemplate.opsForValue().set(fightKey(fightUuid), fight, FIGHT_TTL);
  }

  @Override
  public void removeFight(String fightUuid) {
    redisTemplate.delete(fightKey(fightUuid));
  }

  @Override
  public boolean isUserInFight(String username) {
    return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(USERS_IN_FIGHT_KEY, username));
  }

  @Override
  public void markUserInFight(String username) {
    redisTemplate.opsForSet().add(USERS_IN_FIGHT_KEY, username);
  }

  @Override
  public void markUsersInFight(Collection<String> usernames) {
    usernames.forEach(this::markUserInFight);
  }

  @Override
  public void unmarkUserInFight(String username) {
    redisTemplate.opsForSet().remove(USERS_IN_FIGHT_KEY, username);
  }

  @Override
  public void unmarkUsersInFight(Collection<String> usernames) {
    usernames.forEach(this::unmarkUserInFight);
  }

  private String fightKey(String fightUuid) {
    return "anf:runtime:fight:" + fightUuid;
  }
}
