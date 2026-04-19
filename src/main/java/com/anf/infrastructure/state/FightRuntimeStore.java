package com.anf.infrastructure.state;

import com.anf.service.fight.model.Fight;
import java.util.Collection;
import java.util.Optional;

public interface FightRuntimeStore {
  Optional<Fight> getFight(String fightUuid);

  void saveFight(String fightUuid, Fight fight);

  void removeFight(String fightUuid);

  boolean isUserInFight(String username);

  void markUserInFight(String username);

  void markUsersInFight(Collection<String> usernames);

  void unmarkUserInFight(String username);

  void unmarkUsersInFight(Collection<String> usernames);
}
