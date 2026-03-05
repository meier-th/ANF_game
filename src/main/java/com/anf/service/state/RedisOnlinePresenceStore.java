package com.anf.service.state;

import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RedisOnlinePresenceStore implements OnlinePresenceStore {
  private static final String ONLINE_USERS_KEY = "anf:online-users";

  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public void markOnline(String username) {
    redisTemplate.opsForZSet().add(ONLINE_USERS_KEY, username, System.currentTimeMillis());
  }

  @Override
  public void markOffline(String username) {
    redisTemplate.opsForZSet().remove(ONLINE_USERS_KEY, username);
  }

  @Override
  public List<String> listOnlineUsers() {
    var users = redisTemplate.opsForZSet().range(ONLINE_USERS_KEY, 0, -1);
    if (users == null) {
      return List.of();
    }
    return users.stream().map(String.class::cast).toList();
  }

  @Override
  public List<String> removeStale(Duration inactiveFor) {
    var threshold = System.currentTimeMillis() - inactiveFor.toMillis();
    ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
    var staleUsers = zSetOperations.rangeByScore(ONLINE_USERS_KEY, 0, threshold);
    if (staleUsers == null || staleUsers.isEmpty()) {
      return List.of();
    }
    staleUsers.forEach((user) -> zSetOperations.remove(ONLINE_USERS_KEY, user));
    return staleUsers.stream().map(String.class::cast).toList();
  }
}
