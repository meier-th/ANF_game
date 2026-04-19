package com.anf.service;

import com.anf.model.Fight;
import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
import com.anf.service.state.FightStateStore;
import com.anf.service.state.FightStore;
import com.anf.service.state.proto.GameStateModels.AttackEvent;
import com.anf.service.state.proto.GameStateModels.AttackEventType;
import com.anf.service.state.proto.GameStateModels.CreatureStatus;
import com.anf.service.state.proto.GameStateModels.TakenTurn;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FightSnapshotService {
  public enum TimeoutReportResult {
    TIMED_OUT,
    ALREADY_PROCESSED,
    NOT_TIMED_OUT_YET,
    FIGHT_NOT_FOUND
  }

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

  public void initializeCurrentTurn(String fightUuid, Fight fight, long nowMs) {
    protobufFightStateStore.updateFightState(
        fightUuid,
        (currentState) ->
            currentState.toBuilder()
                .setCurrentAttackerId(fight.getCurrentAttacker(0))
                .setCurrentTurnStartedAtMs(nowMs)
                .build());
  }

  public boolean isCurrentAttacker(String fightUuid, String attackerId, String fallbackCurrentAttacker) {
    var fightState = protobufFightStateStore.getFightState(fightUuid);
    if (fightState.isEmpty()) {
      return false;
    }
    var currentAttacker = fightState.get().getCurrentAttackerId();
    if (currentAttacker == null || currentAttacker.isBlank()) {
      return attackerId.equals(fallbackCurrentAttacker);
    }
    return attackerId.equals(currentAttacker);
  }

  public void registerExecutedTurn(
      String fightUuid, String attackerId, String nextAttackerId, long nowMs, long turnStartedAtMs) {
    protobufFightStateStore.updateFightState(
        fightUuid,
        (currentState) ->
            currentState.toBuilder()
                .addAttackLog(
                    AttackEvent.newBuilder()
                        .setAttackerId(attackerId)
                        .setEventType(AttackEventType.ATTACK_EVENT_TYPE_EXECUTED)
                        .setTimestamp(nowMs)
                        .setTurnStartedAtMs(turnStartedAtMs)
                        .build())
                .setCurrentAttackerId(nextAttackerId)
                .setCurrentTurnStartedAtMs(nowMs)
                .build());
  }

  public TimeoutReportResult timeoutCurrentTurnIfExpired(
      String fightUuid, String expectedAttackerId, String nextAttackerId, long nowMs, long timeoutMs) {
    var stateUpdateResult =
        protobufFightStateStore.updateFightState(
            fightUuid,
            (currentState) -> {
              if (!expectedAttackerId.equals(currentState.getCurrentAttackerId())) {
                return currentState;
              }
              var elapsed = nowMs - currentState.getCurrentTurnStartedAtMs();
              if (elapsed < timeoutMs) {
                return currentState;
              }
              return currentState.toBuilder()
                  .addAttackLog(
                      AttackEvent.newBuilder()
                          .setAttackerId(expectedAttackerId)
                          .setEventType(AttackEventType.ATTACK_EVENT_TYPE_TIMED_OUT)
                          .setTimestamp(nowMs)
                          .setTurnStartedAtMs(currentState.getCurrentTurnStartedAtMs())
                          .build())
                  .setCurrentAttackerId(nextAttackerId)
                  .setCurrentTurnStartedAtMs(nowMs)
                  .build();
            });

    if (stateUpdateResult == FightStateStore.FightStateUpdateResult.FIGHT_STATE_NOT_FOUND) {
      return TimeoutReportResult.FIGHT_NOT_FOUND;
    }
    var updatedState = protobufFightStateStore.getFightState(fightUuid);
    if (updatedState.isEmpty()) {
      return TimeoutReportResult.FIGHT_NOT_FOUND;
    }
    if (nextAttackerId.equals(updatedState.get().getCurrentAttackerId())) {
      var logSize = updatedState.get().getAttackLogCount();
      if (logSize > 0) {
        var event = updatedState.get().getAttackLog(logSize - 1);
        if (event.getEventType() == AttackEventType.ATTACK_EVENT_TYPE_TIMED_OUT
            && expectedAttackerId.equals(event.getAttackerId())
            && event.getTimestamp() == nowMs) {
          return TimeoutReportResult.TIMED_OUT;
        }
      }
      return TimeoutReportResult.ALREADY_PROCESSED;
    }
    return TimeoutReportResult.NOT_TIMED_OUT_YET;
  }

  public long currentTurnStartedAt(String fightUuid) {
    return protobufFightStateStore
        .getFightState(fightUuid)
        .map((state) -> state.getCurrentTurnStartedAtMs())
        .orElse(0L);
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
