package com.anf.service;

import com.anf.model.Attack;
import com.anf.model.Fight;
import com.anf.model.database.FightPVP;
import com.anf.service.state.LegacyFightRuntimeStore;
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
  private final LegacyFightRuntimeStore fightStateStore;
  private final InMemoryFightTurnScheduler fightTurnScheduler;

  public ResponseEntity<?> attack(
      AttackContext context,
      Function<AttackContext, Attack> pvpAttack,
      Function<AttackContext, Attack> pveAttack,
      Runnable scheduleNextTurn) {
    if (!fightSnapshotService.hasProtobufState(context.fightUuid())) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\n\"code\": 2\n}");
    }

    Fight fight = fightStateStore.getFight(context.fightUuid()).orElse(null);
    if (fight == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\n\"code\": 2\n}"); // code 2 means fight doesn't exist
    }

    if (!context.attackerName().equals(fight.getCurrentAttacker(0))) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"code\": 10}"); // 10 - not your turn
    }

    fightTurnScheduler.cancel(context.fightUuid());
    Attack attack = fight instanceof FightPVP ? pvpAttack.apply(context) : pveAttack.apply(context);
    if (attack.getCode() != 0) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(attack.toString());
    }

    scheduleNextTurn.run();
    fightSnapshotService.syncFightSnapshot(context.fightUuid(), fight);
    return ResponseEntity.status(HttpStatus.OK).body(attack.toString());
  }
}
