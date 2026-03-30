package com.anf.service.state;

import com.anf.service.state.proto.GameStateModels.FightState;
import java.util.Optional;
import java.util.function.UnaryOperator;

public interface FightStateStore {
  int MAX_TRANSACTION_RETRIES = 5;

  enum FightStateUpdateResult {
    UPDATED,
    FIGHT_STATE_NOT_FOUND,
    TRANSACTION_CONFLICT
  }

  void createFightState(FightState fightState);

  Optional<FightState> getFightState(String fightUuid);

  FightStateUpdateResult updateFightState(String fightUuid, UnaryOperator<FightState> updater);

  void deleteFightState(String fightUuid);
}
