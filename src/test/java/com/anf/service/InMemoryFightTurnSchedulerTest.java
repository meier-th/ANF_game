package com.anf.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryFightTurnSchedulerTest {
  private InMemoryFightTurnScheduler scheduler;

  @BeforeEach
  void setUp() {
    scheduler = new InMemoryFightTurnScheduler();
  }

  @Test
  void schedule_executesTaskAndExposesDelay() throws InterruptedException {
    var latch = new CountDownLatch(1);

    scheduler.schedule("fight-1", latch::countDown, 10, TimeUnit.MILLISECONDS);

    assertThat(scheduler.delayMillis("fight-1")).isPresent();
    assertThat(latch.await(500, TimeUnit.MILLISECONDS)).isTrue();
  }

  @Test
  void remove_deletesExistingTimerReference() {
    scheduler.schedule("fight-1", () -> {}, 1, TimeUnit.SECONDS);
    scheduler.remove("fight-1");
    assertThat(scheduler.delayMillis("fight-1")).isEmpty();
  }
}
