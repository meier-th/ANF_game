package com.anf.domain.fight;

import com.anf.configuration.WebSocketsController;
import com.anf.domain.fight.model.Fight;
import com.anf.domain.shared.ApiAnswer;
import com.anf.domain.shared.ApiField;
import com.anf.domain.shared.ApiMessage;
import com.anf.domain.shared.ErrorCode;
import com.anf.domain.shared.GameplayConstants;
import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
import com.anf.infrastructure.state.FightRuntimeStore;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class FightTurnEngineService {
  private static final long TURN_TIMEOUT_MILLIS = 30_000L;

  public enum TimeoutFlowResult {
    TIMED_OUT,
    ALREADY_PROCESSED,
    NOT_TIMED_OUT_YET,
    FIGHT_NOT_FOUND,
    NOT_PARTICIPANT
  }

  private final WebSocketsController webSocketsController;
  private final FightRuntimeStore fightStateStore;
  private final FightSnapshotService fightSnapshotService;
  private final BossTurnService bossTurnService;
  private final AnimalTurnService animalTurnService;

  public void schedule(Fight fight, String fightUuid, boolean first) {
    var now = System.currentTimeMillis();
    var previousAttacker = first ? null : fight.getCurrentAttacker(0);
    var previousTurnStartedAt = first ? now : fightSnapshotService.currentTurnStartedAt(fightUuid);

    switchAttacker(fight, first);
    fightStateStore.saveFight(fightUuid, fight);
    fightSnapshotService.syncFightSnapshot(fightUuid, fight);
    if (first) {
      fightSnapshotService.initializeCurrentTurn(fightUuid, fight, now);
    } else {
      fightSnapshotService.registerExecutedTurn(
          fightUuid, previousAttacker, fight.getCurrentAttacker(0), now, previousTurnStartedAt);
    }

    notifySwitch(fight);
    processAiTurnsIfNeeded(fight, fightUuid);
  }

  public ResponseEntity<?> timeoutCurrentTurn(String fightUuid, String reporter, String timedOutAttacker) {
    var runtimeFight = fightStateStore.getFight(fightUuid).orElse(null);
    if (runtimeFight == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              "{\"code\":"
                  + ErrorCode.NOT_FOUND.getValue()
                  + ",\"error\":\""
                  + ApiMessage.FIGHT_NOT_FOUND.getValue()
                  + "\"}");
    }
    if (!isParticipant(runtimeFight, reporter)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              "{\"code\":"
                  + ErrorCode.FORBIDDEN.getValue()
                  + ",\"error\":\""
                  + ApiMessage.NOT_A_PARTICIPANT.getValue()
                  + "\"}");
    }
    var currentAttacker = runtimeFight.getCurrentAttacker(0);
    if (!currentAttacker.equals(timedOutAttacker)) {
      return ResponseEntity.ok(
          java.util.Map.of(ApiField.ANSWER.getValue(), ApiAnswer.ALREADY_PROCESSED.getValue()));
    }
    var now = System.currentTimeMillis();
    var turnAgeMillis = now - fightSnapshotService.currentTurnStartedAt(fightUuid);
    if (turnAgeMillis < TURN_TIMEOUT_MILLIS) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              java.util.Map.of(
                  ApiField.CODE.getValue(),
                  ErrorCode.CONFLICT.getValue(),
                  ApiField.ERROR.getValue(),
                  ApiMessage.CURRENT_TURN_NOT_TIMED_OUT.getValue()));
    }
    var nextAttacker = nextAttackerToken(runtimeFight);
    var timeoutResult =
        fightSnapshotService.timeoutCurrentTurnIfExpired(
            fightUuid, timedOutAttacker, nextAttacker, now, TURN_TIMEOUT_MILLIS);
    if (timeoutResult == FightSnapshotService.TimeoutReportResult.FIGHT_NOT_FOUND) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              "{\"code\":"
                  + ErrorCode.NOT_FOUND.getValue()
                  + ",\"error\":\""
                  + ApiMessage.FIGHT_NOT_FOUND.getValue()
                  + "\"}");
    }
    if (timeoutResult == FightSnapshotService.TimeoutReportResult.ALREADY_PROCESSED) {
      return ResponseEntity.ok(
          java.util.Map.of(ApiField.ANSWER.getValue(), ApiAnswer.ALREADY_PROCESSED.getValue()));
    }

    switchAttacker(runtimeFight, false);
    fightStateStore.saveFight(fightUuid, runtimeFight);
    fightSnapshotService.syncFightSnapshot(fightUuid, runtimeFight);
    notifySwitch(runtimeFight);
    processAiTurnsIfNeeded(runtimeFight, fightUuid);
    return ResponseEntity.ok(
        java.util.Map.of(
            ApiField.ANSWER.getValue(),
            ApiAnswer.TIMED_OUT.getValue(),
            ApiField.NEXT_ATTACKER.getValue(),
            runtimeFight.getCurrentAttacker(0)));
  }

  private String switchAttacker(Fight fight, boolean firstTurn) {
    if (firstTurn) {
      fight.switchAttacker();
    } else {
      if (fight instanceof FightVsAI) {
        String current = fight.getCurrentAttacker(0);
        fight.switchAttacker();
        if (current.equals(fight.getCurrentAttacker(0))) {
          fight.switchAttacker();
        }
      } else {
        fight.switchAttacker();
      }
    }
    return fight.getCurrentAttacker(0);
  }

  private void notifySwitch(Fight fight) {
    log.debug("Attacker switched to {}", fight.getCurrentAttacker(0));

    if (fight instanceof FightPVP) {
      webSocketsController.sendSwitch(((FightPVP) fight).getFighter1().getLogin(), fight.getCurrentAttacker(0));
      webSocketsController.sendSwitch(((FightPVP) fight).getFighter2().getLogin(), fight.getCurrentAttacker(0));
    } else {
      fight.getFighters().stream()
          .map((user) -> user != null ? user.getLogin() : null)
          .filter((login) -> login != null && !login.isBlank())
          .forEach((login) -> webSocketsController.sendSwitch(login, fight.getCurrentAttacker(0)));
    }
  }

  private void processAiTurnsIfNeeded(Fight fight, String fightUuid) {
    if (fight instanceof FightPVP pvpFight
        && fight.getCurrentAttacker(0).length() == GameplayConstants.PVP_ANIMAL_TOKEN_LENGTH) {
      animalPvpAttack(pvpFight, fightUuid);
      return;
    }
    if (fight instanceof FightVsAI pveFight) {
      if (fight.getCurrentAttacker(0).length() <= GameplayConstants.PVE_BOSS_TOKEN_MAX_LENGTH) {
        bossAttack(pveFight, fightUuid);
      } else if (fight.getCurrentAttacker(0).length() >= GameplayConstants.PVE_ANIMAL_TOKEN_MIN_LENGTH
          && fight.getCurrentAttacker(0).length() <= GameplayConstants.PVE_ANIMAL_TOKEN_MAX_LENGTH) {
        animalPveAttack(pveFight, fightUuid);
      }
    }
  }

  public void bossAttack(FightVsAI fight, String fightUuid) {
    bossTurnService.handleBossAttack(fight, fightUuid, () -> schedule(fight, fightUuid, false));
  }

  public void animalPvpAttack(FightPVP fight, String fightUuid) {
    animalTurnService.handleAnimalPvpAttack(fight, fightUuid, () -> schedule(fight, fightUuid, false));
  }

  public void animalPveAttack(FightVsAI fight, String fightUuid) {
    animalTurnService.handleAnimalPveAttack(fight, fightUuid, () -> schedule(fight, fightUuid, false));
  }

  private String nextAttackerToken(Fight fight) {
    var currentAttacker = fight.getCurrentAttacker(0);
    var nextAttacker = fight.getNextAttacker();
    if (fight instanceof FightVsAI && currentAttacker.equals(nextAttacker)) {
      return fight.getCurrentAttacker(2);
    }
    return nextAttacker;
  }

  private boolean isParticipant(Fight fight, String username) {
    if (fight instanceof FightPVP pvp) {
      return pvp.getFighter1().getLogin().equals(username) || pvp.getFighter2().getLogin().equals(username);
    }
    if (fight instanceof FightVsAI pve) {
      return pve.getFighters().stream()
          .anyMatch((participant) -> participant != null && username.equals(participant.getLogin()));
    }
    return false;
  }
}
