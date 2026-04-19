package com.anf.domain.fight;

import com.anf.configuration.WebSocketsController;
import com.anf.domain.fight.model.State;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FightStateNotifier {
  private final WebSocketsController webSocketsController;

  public void sendAfterAttack(
      String username,
      int damage,
      String targetName,
      String attacker,
      String next,
      boolean dead,
      boolean allDead,
      String attackName,
      int chakraCost,
      int chakraBurn) {
    State state =
        new State(attacker, targetName, attackName, chakraCost, damage, chakraBurn, dead, allDead, next);
    webSocketsController.sendFightState(state, username);
  }
}
