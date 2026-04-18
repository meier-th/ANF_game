package com.anf.service;

import com.anf.model.Fight;
import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
import com.anf.service.state.FightStateStore;
import com.anf.service.state.FightStore;
import com.anf.service.state.proto.GameStateModels.CreatureStatus;
import com.anf.service.state.proto.GameStateModels.TakenTurn;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FightSnapshotService {
  private final FightStore protobufFightStore;
  private final FightStateStore protobufFightStateStore;

  public boolean hasProtobufState(String fightUuid) {
    return protobufFightStore.getFight(fightUuid).isPresent()
        && protobufFightStateStore.getFightState(fightUuid).isPresent();
  }

  public void syncFightSnapshot(String fightUuid, Fight fight) {
    protobufFightStateStore.updateFightState(
        fightUuid,
        (currentState) -> {
          var builder = currentState.toBuilder();
          builder.clearCreatureStatuses().putAllCreatureStatuses(captureStatuses(fight));
          var currentAttacker = fight.getCurrentAttacker(0);
          if (currentAttacker != null) {
            builder.addTakenTurns(
                TakenTurn.newBuilder()
                    .setCharacterUuid(currentAttacker)
                    .setTimestamp(System.currentTimeMillis())
                    .build());
          }
          return builder.build();
        });
  }

  public void deleteFightArtifacts(String fightUuid, Runnable deleteLegacyFightState) {
    deleteLegacyFightState.run();
    protobufFightStore.deleteFight(fightUuid);
    protobufFightStateStore.deleteFightState(fightUuid);
  }

  private Map<String, CreatureStatus> captureStatuses(Fight fight) {
    var statuses = new HashMap<String, CreatureStatus>();
    if (fight instanceof FightPVP pvp) {
      statuses.put(
          pvp.getFighter1().getLogin(),
          CreatureStatus.newBuilder()
              .setRemainingHealth(pvp.getFighter1().getCharacter().getCurrentHP())
              .build());
      statuses.put(
          pvp.getFighter2().getLogin(),
          CreatureStatus.newBuilder()
              .setRemainingHealth(pvp.getFighter2().getCharacter().getCurrentHP())
              .build());
      return statuses;
    }
    if (fight instanceof FightVsAI pve) {
      pve.getFighters()
          .forEach(
              (fighter) ->
                  statuses.put(
                      fighter.getLogin(),
                      CreatureStatus.newBuilder()
                          .setRemainingHealth(fighter.getCharacter().getCurrentHP())
                          .build()));
      statuses.put(
          "boss:" + pve.getBoss().getNumberOfTails(),
          CreatureStatus.newBuilder().setRemainingHealth(pve.getBoss().getCurrentHP()).build());
    }
    return statuses;
  }
}
