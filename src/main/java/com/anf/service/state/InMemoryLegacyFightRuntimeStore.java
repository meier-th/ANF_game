package com.anf.service.state;

import com.anf.model.Fight;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class InMemoryLegacyFightRuntimeStore implements LegacyFightRuntimeStore {
  private final AtomicInteger queueSequence = new AtomicInteger();
  private final ConcurrentHashMap<Integer, ArrayDeque<String>> queues = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Fight> fights = new ConcurrentHashMap<>();
  private final ConcurrentSkipListSet<String> usersInFight = new ConcurrentSkipListSet<>();

  @Override
  public int nextQueueId() {
    return queueSequence.getAndIncrement();
  }

  @Override
  public void createQueue(int queueId, String owner) {
    var queue = new ArrayDeque<String>();
    queue.add(owner);
    queues.put(queueId, queue);
  }

  @Override
  public boolean queueExists(int queueId) {
    return queues.containsKey(queueId);
  }

  @Override
  public int queueSize(int queueId) {
    var queue = queues.get(queueId);
    return queue == null ? 0 : queue.size();
  }

  @Override
  public List<String> queueUsers(int queueId) {
    var queue = queues.get(queueId);
    return queue == null ? List.of() : List.copyOf(queue);
  }

  @Override
  public void addUserToQueue(int queueId, String username) {
    queues.computeIfPresent(queueId, (key, queue) -> {
      queue.add(username);
      return queue;
    });
  }

  @Override
  public String popQueueUser(int queueId) {
    var queue = queues.get(queueId);
    return queue == null ? null : queue.poll();
  }

  @Override
  public void removeQueue(int queueId) {
    queues.remove(queueId);
  }

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
