package com.anf.service.state;

import com.anf.model.Fight;
import java.util.Collection;
import java.util.Optional;

public interface LegacyFightRuntimeStore {
  Optional<Fight> getFight(String fightUuid);

  void saveFight(String fightUuid, Fight fight);

  void removeFight(String fightUuid);

  boolean isUserInFight(String username);

  void markUserInFight(String username);

  void markUsersInFight(Collection<String> usernames);

  void unmarkUserInFight(String username);

  void unmarkUsersInFight(Collection<String> usernames);
}
