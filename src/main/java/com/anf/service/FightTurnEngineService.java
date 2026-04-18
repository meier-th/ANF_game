package com.anf.service;

import com.anf.config.WebSocketsController;
import com.anf.model.Fight;
import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
import com.anf.service.state.LegacyFightRuntimeStore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FightTurnEngineService {
  private final WebSocketsController webSocketsController;
  private final LegacyFightRuntimeStore fightStateStore;
  private final FightSnapshotService fightSnapshotService;
  private final InMemoryFightTurnScheduler fightTurnScheduler;
  private final BossTurnService bossTurnService;
  private final AnimalTurnService animalTurnService;

  public void schedule(Fight fight, String fightUuid, boolean first) {
    if (first) {
      fight.switchAttacker();
    } else {
      if (fight instanceof FightVsAI) {
        String current = fight.getCurrentAttacker(0);
        fight.switchAttacker();
        if (current.equals(fight.getCurrentAttacker(0))) {
          fight.switchAttacker();
        }
      }
    }
    System.out.println(
        "Attacker switched: to "
            + fight.getCurrentAttacker(0)
            + " At: "
            + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
            + '\n');

    if (fight instanceof FightPVP) {
      webSocketsController.sendSwitch(((FightPVP) fight).getFighter1().getLogin(), fight.getCurrentAttacker(0));
      webSocketsController.sendSwitch(((FightPVP) fight).getFighter2().getLogin(), fight.getCurrentAttacker(0));
      if (fight.getCurrentAttacker(0).length() == 4) {
        animalPvpAttack((FightPVP) fight, fightUuid);
      }
    } else {
      ((FightVsAI) fight)
          .getSetFighters()
          .forEach(
              (user) ->
                  webSocketsController.sendSwitch(
                      user.getFighter().getUser().getLogin(), fight.getCurrentAttacker(0)));
      if (fight.getCurrentAttacker(0).length() < 3) {
        bossTurnService.handleBossAttack((FightVsAI) fight, fightUuid, () -> schedule(fight, fightUuid, false));
      } else if (fight.getCurrentAttacker(0).length() >= 3 && fight.getCurrentAttacker(0).length() < 5) {
        animalTurnService.handleAnimalPveAttack(
            (FightVsAI) fight, fightUuid, () -> schedule(fight, fightUuid, false));
      }
    }
    fightStateStore.saveFight(fightUuid, fight);
    fightSnapshotService.syncFightSnapshot(fightUuid, fight);
    if (fight.getCurrentAttacker(0).length() >= 6) {
      fightTurnScheduler.schedule(fightUuid, () -> schedule(fight, fightUuid, false), 30, TimeUnit.SECONDS);
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
}
