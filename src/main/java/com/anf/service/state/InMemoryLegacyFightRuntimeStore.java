package com.anf.service.state;

import com.anf.model.Fight;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.springframework.stereotype.Service;

@Service
public class InMemoryLegacyFightRuntimeStore implements LegacyFightRuntimeStore {
  private final ConcurrentHashMap<String, Fight> fights = new ConcurrentHashMap<>();
  private final ConcurrentSkipListSet<String> usersInFight = new ConcurrentSkipListSet<>();

  @Override
  public Optional<Fight> getFight(String fightUuid) {
    return Optional.ofNullable(fights.get(fightUuid));
  }

  @Override
  public void saveFight(String fightUuid, Fight fight) {
    fights.put(fightUuid, fight);
  }

  @Override
  public void removeFight(String fightUuid) {
    fights.remove(fightUuid);
  }

  @Override
  public boolean isUserInFight(String username) {
    return usersInFight.contains(username);
  }

  @Override
  public void markUserInFight(String username) {
    usersInFight.add(username);
  }

  @Override
  public void markUsersInFight(Collection<String> usernames) {
    usersInFight.addAll(usernames);
  }

  @Override
  public void unmarkUserInFight(String username) {
    usersInFight.remove(username);
  }

  @Override
  public void unmarkUsersInFight(Collection<String> usernames) {
    usernames.forEach(usersInFight::remove);
  }
}
