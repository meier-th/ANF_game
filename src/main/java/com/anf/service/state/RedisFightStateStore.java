package com.anf.service.state;

import com.anf.model.Fight;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RedisFightStateStore implements FightStateStore {
  private static final String FIGHT_KEY_PREFIX = "anf:fight:";
  private static final String QUEUE_KEY_PREFIX = "anf:queue:";
  private static final String USERS_IN_FIGHT_KEY = "anf:users-in-fight";
  private static final String QUEUE_SEQUENCE_KEY = "anf:queue-sequence";
  private static final Duration FIGHT_TTL = Duration.ofHours(2);
  private static final Duration QUEUE_TTL = Duration.ofMinutes(30);

  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public int nextQueueId() {
    var next = redisTemplate.opsForValue().increment(QUEUE_SEQUENCE_KEY);
    return next == null ? 0 : next.intValue();
  }

  @Override
  public void createQueue(int queueId, String owner) {
    var key = queueKey(queueId);
    redisTemplate.delete(key);
    redisTemplate.opsForList().rightPush(key, owner);
    redisTemplate.expire(key, QUEUE_TTL);
  }

  @Override
  public boolean queueExists(int queueId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(queueKey(queueId)));
  }

  @Override
  public int queueSize(int queueId) {
    var size = redisTemplate.opsForList().size(queueKey(queueId));
    return size == null ? 0 : size.intValue();
  }

  @Override
  public List<String> queueUsers(int queueId) {
    var values = redisTemplate.opsForList().range(queueKey(queueId), 0, -1);
    if (values == null) {
      return List.of();
    }
    return values.stream().map(String.class::cast).toList();
  }

  @Override
  public void addUserToQueue(int queueId, String username) {
    var key = queueKey(queueId);
    redisTemplate.opsForList().rightPush(key, username);
    redisTemplate.expire(key, QUEUE_TTL);
  }

  @Override
  public String popQueueUser(int queueId) {
    var value = redisTemplate.opsForList().leftPop(queueKey(queueId));
    return value == null ? null : (String) value;
  }

  @Override
  public void removeQueue(int queueId) {
    redisTemplate.delete(queueKey(queueId));
  }

  @Override
  public Optional<Fight> getFight(int fightId) {
    var fight = redisTemplate.opsForValue().get(fightKey(fightId));
    return Optional.ofNullable((Fight) fight);
  }

  @Override
  public void saveFight(Fight fight) {
    redisTemplate.opsForValue().set(fightKey(fight.getId()), fight, FIGHT_TTL);
  }

  @Override
  public void removeFight(int fightId) {
    redisTemplate.delete(fightKey(fightId));
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

  private String fightKey(int fightId) {
    return FIGHT_KEY_PREFIX + fightId;
  }

  private String queueKey(int queueId) {
    return QUEUE_KEY_PREFIX + queueId;
  }
}
