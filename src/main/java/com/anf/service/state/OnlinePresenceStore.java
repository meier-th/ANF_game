package com.anf.service.state;

import java.time.Duration;
import java.util.List;

public interface OnlinePresenceStore {

  void markOnline(String username);

  void markOffline(String username);

  List<String> listOnlineUsers();

  List<String> removeStale(Duration inactiveFor);
}
