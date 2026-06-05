package com.anf.domain.fight;

import com.anf.domain.combat.FightStatsUpdateService;
import com.anf.domain.fight.model.Fight;
import com.anf.domain.shared.ApiField;
import com.anf.domain.shared.ErrorCode;
import com.anf.infrastructure.state.FightRuntimeStore;
import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FightSurrenderService {
  private final FightRuntimeStore fightRuntimeStore;
  private final FightSnapshotService fightSnapshotService;
  private final FightStateNotifier fightStateNotifier;
  private final FightStatsUpdateService fightStatsUpdateService;

  public ResponseEntity<?> surrender(String fightUuid, String username) {
    if (!fightSnapshotService.hasProtobufState(fightUuid)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\n\"" + ApiField.CODE.getValue() + "\": " + ErrorCode.NOT_FOUND.getValue() + "\n}");
    }
    Fight fight = fightRuntimeStore.getFight(fightUuid).orElse(null);
    if (fight == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\n\"" + ApiField.CODE.getValue() + "\": " + ErrorCode.NOT_FOUND.getValue() + "\n}");
    }
    if (fight instanceof FightPVP pvpFight) {
      return surrenderPvp(fightUuid, username, pvpFight);
    }
    if (fight instanceof FightVsAI pveFight) {
      return surrenderPve(fightUuid, username, pveFight);
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body("{\n\"" + ApiField.CODE.getValue() + "\": " + ErrorCode.INVALID_REQUEST.getValue() + "\n}");
  }

  private ResponseEntity<?> surrenderPvp(String fightUuid, String username, FightPVP fight) {
    var firstLogin = fight.getFighter1() != null ? fight.getFighter1().getLogin() : null;
    var secondLogin = fight.getFighter2() != null ? fight.getFighter2().getLogin() : null;
    if (firstLogin == null || secondLogin == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\n\"" + ApiField.CODE.getValue() + "\": " + ErrorCode.NOT_FOUND.getValue() + "\n}");
    }
    boolean firstSurrendered = Objects.equals(firstLogin, username);
    boolean secondSurrendered = Objects.equals(secondLogin, username);
    if (!firstSurrendered && !secondSurrendered) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("{\n\"" + ApiField.CODE.getValue() + "\": " + ErrorCode.FORBIDDEN.getValue() + "\n}");
    }

    fightStatsUpdateService.finalizePvpFight(fight, secondSurrendered);
    fightStateNotifier.sendAfterAttack(firstLogin, 0, username, username, "", true, true, "Surrender", 0, 0);
    fightStateNotifier.sendAfterAttack(secondLogin, 0, username, username, "", true, true, "Surrender", 0, 0);

    fightRuntimeStore.unmarkUserInFight(firstLogin);
    fightRuntimeStore.unmarkUserInFight(secondLogin);
    fightSnapshotService.deleteFightArtifacts(fightUuid, () -> fightRuntimeStore.removeFight(fightUuid));

    return ResponseEntity.ok("{\"result\":\"ok\"}");
  }

  private ResponseEntity<?> surrenderPve(String fightUuid, String username, FightVsAI fight) {
    var participant =
        fight.getFighters().stream().anyMatch((fighter) -> Objects.equals(fighter.getLogin(), username));
    if (!participant) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("{\n\"" + ApiField.CODE.getValue() + "\": " + ErrorCode.FORBIDDEN.getValue() + "\n}");
    }

    for (var fighter : fight.getFighters()) {
      fightStateNotifier.sendAfterAttack(
          fighter.getLogin(), 0, username, username, "", true, true, "Surrender", 0, 0);
    }
    fightStatsUpdateService.finalizePvePlayersDefeated(fight);
    fight.getFighters().forEach((fighter) -> fightRuntimeStore.unmarkUserInFight(fighter.getLogin()));
    fightSnapshotService.deleteFightArtifacts(fightUuid, () -> fightRuntimeStore.removeFight(fightUuid));

    return ResponseEntity.ok("{\"result\":\"ok\"}");
  }
}
