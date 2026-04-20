package com.anf.domain.fight;

import com.anf.domain.shared.GameplayConstants;
import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.FightPVP;
import com.anf.model.database.FightVsAI;
import com.anf.domain.combat.BossService;
import com.anf.domain.user.UserService;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FightRuntimeFactoryService {
  private final UserService userService;
  private final BossService bossService;

  public FightPVP createPvpRuntimeFight(List<String> participants) {
    if (participants.size() != 2) {
      return null;
    }
    var fighter1 = userService.getUser(participants.get(0)).getCharacter();
    var fighter2 = userService.getUser(participants.get(1)).getCharacter();
    if (fighter1 == null || fighter2 == null) {
      return null;
    }
    var fight = new FightPVP();
    fighter1.prepareForFight();
    fighter2.prepareForFight();
    fight.setFighters(fighter1, fighter2);
    int biggerRating =
        GameplayConstants.PVP_BASE_RATING_CHANGE
            + Math.abs(fighter1.getUser().getStats().getRating() - fighter2.getUser().getStats().getRating())
                / GameplayConstants.PVP_BIGGER_RATING_GAP_DIVISOR;
    int lesserRating =
        GameplayConstants.PVP_BASE_RATING_CHANGE
            - Math.abs(fighter1.getUser().getStats().getRating() - fighter2.getUser().getStats().getRating())
                / GameplayConstants.PVP_LESSER_RATING_GAP_DIVISOR;
    if (lesserRating < GameplayConstants.PVP_MIN_RATING_CHANGE) {
      lesserRating = GameplayConstants.PVP_MIN_RATING_CHANGE;
    }
    fight.setBiggerRatingChange(biggerRating);
    fight.setLessRatingChange(lesserRating);
    return fight;
  }

  public FightVsAI createPveRuntimeFight(List<String> participants, String bossName) {
    var boss = bossService.getBossByName(bossName);
    if (boss == null) {
      return null;
    }
    var fight = new FightVsAI();
    ArrayList<AiFightParticipation> userFights = new ArrayList<>();
    for (String fighterName : participants) {
      var fighter = userService.getUser(fighterName).getCharacter();
      if (fighter == null) {
        return null;
      }
      AiFightParticipation userF = new AiFightParticipation();
      userF.setFight(fight);
      userF.setFighter(fighter);
      fighter.prepareForFight();
      fight.addFighter(fighter);
      userFights.add(userF);
    }
    fight.setSetFighters(userFights);
    boss.prepareForFight();
    fight.setBoss(boss);
    return fight;
  }
}
