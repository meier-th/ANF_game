package com.anf.service.state;

import com.anf.model.Fight;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LegacyFightRuntimeStore {
  int nextQueueId();

  void createQueue(int queueId, String owner);

  boolean queueExists(int queueId);

  int queueSize(int queueId);

  List<String> queueUsers(int queueId);

  void addUserToQueue(int queueId, String username);

  String popQueueUser(int queueId);

  void removeQueue(int queueId);

  Optional<Fight> getFight(String fightUuid);

  void saveFight(String fightUuid, Fight fight);

  void removeFight(String fightUuid);

  boolean isUserInFight(String username);

  void markUserInFight(String username);

  void markUsersInFight(Collection<String> usernames);

  void unmarkUserInFight(String username);

  void unmarkUsersInFight(Collection<String> usernames);
}
