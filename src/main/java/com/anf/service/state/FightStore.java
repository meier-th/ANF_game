package com.anf.service.state;

import com.anf.service.state.proto.GameStateModels.Fight;
import java.util.Optional;

public interface FightStore {
  void createFight(Fight fight);

  Optional<Fight> getFight(String fightUuid);
}
