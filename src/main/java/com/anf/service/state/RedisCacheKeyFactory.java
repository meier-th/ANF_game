package com.anf.service.state;

import com.anf.service.state.proto.GameStateModels.CacheKey;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheKeyFactory {
  public byte[] lobbyKey(String lobbyUuid) {
    return CacheKey.newBuilder()
        .setDataset(CacheKey.Dataset.DATASET_LOBBY)
        .setLobbyUuid(lobbyUuid)
        .build()
        .toByteArray();
  }

  public byte[] fightKey(String fightUuid) {
    return CacheKey.newBuilder()
        .setDataset(CacheKey.Dataset.DATASET_FIGHT)
        .setFightUuid(fightUuid)
        .build()
        .toByteArray();
  }

  public byte[] fightStateKey(String fightUuid) {
    return CacheKey.newBuilder()
        .setDataset(CacheKey.Dataset.DATASET_FIGHT_STATE)
        .setFightStateFightUuid(fightUuid)
        .build()
        .toByteArray();
  }

}
