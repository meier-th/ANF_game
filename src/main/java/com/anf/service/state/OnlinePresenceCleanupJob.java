package com.anf.service.state;

import com.anf.config.WebSocketsController;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OnlinePresenceCleanupJob {
  private static final Duration USER_ONLINE_TTL = Duration.ofMinutes(5);

  private final OnlinePresenceStore onlinePresenceStore;
  private final WebSocketsController webSocketsController;

  @Scheduled(fixedDelayString = "${anf.online.cleanup-delay-ms:20000}")
  public void cleanupStaleOnlineUsers() {
    onlinePresenceStore
        .removeStale(USER_ONLINE_TTL)
        .forEach((username) -> webSocketsController.sendOnline(username + ":offline"));
  }
}
