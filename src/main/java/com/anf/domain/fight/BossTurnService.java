package com.anf.domain.fight;

import com.anf.domain.fight.model.NinjaAnimal;
import com.anf.domain.shared.SpellName;
import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.FightVsAI;
import com.anf.model.database.User;
import com.anf.domain.combat.FightDamageService;
import com.anf.domain.combat.FightStatsUpdateService;
import com.anf.infrastructure.state.FightRuntimeStore;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class BossTurnService {
  private final FightRuntimeStore fightStateStore;
  private final FightSnapshotService fightSnapshotService;
  private final FightStateNotifier fightStateNotifier;
  private final FightDamageService fightDamageService;
  private final FightStatsUpdateService fightStatsUpdateService;

  public void handleBossAttack(FightVsAI fight, String fightUuid, Runnable scheduleNextTurn) {
    var participations = fight.getSetFighters();
    Set<String> recipientLogins = new LinkedHashSet<>();
    fight.getFighters().stream()
        .map(User::getLogin)
        .filter((login) -> login != null && !login.isBlank())
        .forEach(recipientLogins::add);

    int targetNum = (int) (Math.random() * (fight.getFighters().size() + fight.getAnimals1().size() - 0.5));
    boolean targetUser = targetNum < fight.getFighters().size();
    User target = targetUser ? fight.getFighters().get(targetNum) : null;
    NinjaAnimal targetAnimal = targetUser ? null : fight.getAnimals1().get(targetNum - fight.getFighters().size());

    int damage =
        fightDamageService.computeBossAttackDamage(
            fight.getBoss().getNumberOfTails(),
            targetUser ? target.getCharacter().getResistance() : targetAnimal.getResistance());
    boolean deadly;
    if (targetUser) {
      target.getCharacter().acceptDamage(damage);
      log.debug("Boss attack in fight {} caused {} damage to {}", fightUuid, damage, target.getLogin());
      deadly = target.getCharacter().getCurrentHP() <= 0;
      if (deadly) {
        var targetLogin = target.getLogin();
        var targetCharacterId = target.getCharacter() != null ? target.getCharacter().getId() : -1;
        participations.forEach(
            (set) -> {
              if (set == null || set.getFighter() == null) {
                return;
              }
              var fighterUser = set.getFighter().getUser();
              var loginMatches =
                  fighterUser != null
                      && fighterUser.getLogin() != null
                      && fighterUser.getLogin().equals(targetLogin);
              var idMatches = targetCharacterId > 0 && set.getFighter().getId() == targetCharacterId;
              if (loginMatches || idMatches) {
                set.setResult(AiFightParticipation.Result.DIED);
              }
            });

        for (int i = 0; i < fight.getFighters().size(); i++) {
          if (fight.getFighters().get(i).getLogin().equals(target.getLogin())) {
            log.debug("Boss killed fighter {}", fight.getFighters().remove(i).getLogin());
            break;
          }
        }
      }
    } else {
      targetAnimal.acceptDamage(damage);
      deadly = targetAnimal.getCurrentHP() <= 0;
      if (deadly) {
        fight.getAnimals1().removeIf((an) -> an.getName().equalsIgnoreCase(targetAnimal.getName()));
      }
    }
    boolean allDead = true;
    for (User user : fight.getFighters()) {
      if (user.getCharacter().getCurrentHP() > 0) allDead = false;
    }
    log.debug("All fighters dead in fight {}: {}", fightUuid, allDead);
    final boolean everyoneDied = allDead;

    recipientLogins.forEach(
        (login) ->
            fightStateNotifier.sendAfterAttack(
                login,
                damage,
                targetUser ? target.getLogin() : targetAnimal.getName().substring(0, 3),
                fight.getCurrentAttacker(0),
                fight.getNextAttacker(),
                deadly,
                everyoneDied,
                SpellName.BOSS_ATTACK.getValue(),
                0,
                0));

    if (allDead) {
      log.info("Fight {} ended with all players defeated", fightUuid);
      fightStatsUpdateService.finalizePvePlayersDefeated(fight);
      recipientLogins.forEach(fightStateStore::unmarkUserInFight);
      fightSnapshotService.deleteFightArtifacts(fightUuid, () -> fightStateStore.removeFight(fightUuid));
      return;
    }
    log.debug("Fight {} continues after boss turn", fightUuid);
    fightStateStore.saveFight(fightUuid, fight);
    scheduleNextTurn.run();
  }
}
