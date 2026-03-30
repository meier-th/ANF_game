package com.anf.service.state;

import java.util.Optional;

public interface FightIdAliasStore {
  void saveMapping(int legacyFightId, String fightUuid);

  Optional<String> getFightUuid(int legacyFightId);

  void deleteMapping(int legacyFightId);
}
