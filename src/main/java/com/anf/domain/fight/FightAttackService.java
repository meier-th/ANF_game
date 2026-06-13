package com.anf.domain.fight;

import com.anf.domain.fight.model.Attack;
import com.anf.domain.fight.model.Fight;
import com.anf.domain.shared.ApiField;
import com.anf.domain.shared.ErrorCode;
import com.anf.model.database.FightPVP;
import com.anf.infrastructure.state.FightRuntimeStore;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FightAttackService {
  public record AttackContext(String attackerName, String enemyName, String fightUuid, String spellName) {}

  private final FightSnapshotService fightSnapshotService;
  private final FightRuntimeStore fightStateStore;

  public ResponseEntity<?> attack(
      AttackContext context,
      Function<AttackContext, Attack> pvpAttack,
      Function<AttackContext, Attack> pveAttack,
      Runnable scheduleNextTurn) {
    if (!fightSnapshotService.hasProtobufState(context.fightUuid())) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\n\"" + ApiField.CODE.getValue() + "\": " + ErrorCode.NOT_FOUND.getValue() + "\n}");
    }

    Fight fight = fightStateStore.getFight(context.fightUuid()).orElse(null);
    if (fight == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\n\"" + ApiField.CODE.getValue() + "\": " + ErrorCode.NOT_FOUND.getValue() + "\n}");
    }

    if (!fightSnapshotService.isCurrentAttacker(
        context.fightUuid(), context.attackerName(), fight.getCurrentAttacker(0))) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("{\"" + ApiField.CODE.getValue() + "\": " + ErrorCode.FORBIDDEN.getValue() + "}");
    }

    Attack attack = fight instanceof FightPVP ? pvpAttack.apply(context) : pveAttack.apply(context);
    if (attack.getCode() != 0) {
      HttpStatus status =
          attack.getCode() == ErrorCode.FORBIDDEN.getValue()
              ? HttpStatus.FORBIDDEN
              : HttpStatus.BAD_REQUEST;
      return ResponseEntity.status(status).body(attack.toString());
    }

    scheduleNextTurn.run();
    fightSnapshotService.syncFightSnapshot(context.fightUuid(), fight);
    return ResponseEntity.status(HttpStatus.OK).body(attack.toString());
  }
}
