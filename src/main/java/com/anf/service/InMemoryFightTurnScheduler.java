package com.anf.service;

import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class InMemoryFightTurnScheduler {
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final ConcurrentHashMap<String, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();

  public void schedule(String fightUuid, Runnable task, long delay, TimeUnit unit) {
    timers.put(fightUuid, scheduler.schedule(task, delay, unit));
  }

  public void cancel(String fightUuid) {
    timers.get(fightUuid).cancel(true);
  }

  public void remove(String fightUuid) {
    timers.remove(fightUuid);
  }

  public OptionalLong delayMillis(String fightUuid) {
    var timer = timers.get(fightUuid);
    if (timer == null) {
      return OptionalLong.empty();
    }
    return OptionalLong.of(timer.getDelay(TimeUnit.MILLISECONDS));
  }
}
