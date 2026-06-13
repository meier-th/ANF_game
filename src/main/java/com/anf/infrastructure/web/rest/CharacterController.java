package com.anf.infrastructure.web.rest;

import com.anf.domain.fight.model.PvpRecord;
import com.anf.domain.fight.model.PveRecord;
import com.anf.domain.combat.SpellKnowledgeService;
import com.anf.domain.shared.ApiField;
import com.anf.domain.shared.ApiMessage;
import com.anf.domain.shared.CharacterUpgradeQuality;
import com.anf.domain.shared.GameplayConstants;
import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.CharacterAppearance;
import com.anf.model.database.FightPVP;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Role;
import com.anf.model.database.Stats;
import com.anf.model.database.User;
import com.anf.infrastructure.persistence.repository.RoleRepository;
import com.anf.domain.user.AppearanceService;
import com.anf.domain.user.CharacterService;
import com.anf.domain.user.StatsService;
import com.anf.domain.user.UserService;
import com.anf.domain.fight.FightVsAIService;
import com.anf.configuration.WebSocketsController;
import com.anf.infrastructure.state.OnlinePresenceStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class CharacterController {

  private final AppearanceService appearanceServ;
  private final CharacterService charServ;
  private final RoleRepository roleRep;
  private final UserService userServ;
  private final StatsService statsServ;
  private final SpellKnowledgeService spellKnowledgeService;
  private final FightVsAIService fightVsAIService;
  private final WebSocketsController wsController;
  private final OnlinePresenceStore onlinePresenceStore;

  @RequestMapping(value = "/")
  public String greeting() {
    return "index";
  }

  @GetMapping("/profile")
  public ResponseEntity<String> myAccount() {
    try {
      User user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
      if (user != null && user.getCharacter() != null) {
        user.getCharacter().setSpellsKnown(spellKnowledgeService.ensureUnlockedSpellKnowledge(user.getCharacter()));
      }
      String response = user.toString();
      return ResponseEntity.status(HttpStatus.OK).body(response);
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.getMessage());
    }
  }

  @GetMapping("/profile/isAdmin")
  public ResponseEntity<?> isAdmin() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean admin =
        authentication != null
            && authentication.getAuthorities().stream()
                .anyMatch(
                    (authority) ->
                        "ROLE_ADMIN".equals(authority.getAuthority())
                            || "ADMIN".equals(authority.getAuthority()));
    return ResponseEntity.ok(Map.of("admin", admin));
  }

  @PostMapping("/profile/character/appearance")
  public ResponseEntity<String> addAppearance(
      @RequestParam String gender,
      @RequestParam String skinColour,
      @RequestParam String hairColour,
      @RequestParam String clothesColour) {
    try {
      User user =
          userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
      GameCharacter ch = user.getCharacter();
      CharacterAppearance appearance =
          new CharacterAppearance(
              CharacterAppearance.Gender.valueOf(gender),
              CharacterAppearance.SkinColour.valueOf(skinColour),
              CharacterAppearance.HairColour.valueOf(hairColour),
              CharacterAppearance.ClothesColour.valueOf(clothesColour));
      ch.setAppearance(appearance);
      appearanceServ.addAppearance(appearance);
      charServ.addCharacter(ch);
      return ResponseEntity.status(HttpStatus.OK)
          .body("{ \"" + ApiField.MSG.getValue() + "\": \"" + ApiMessage.APPEARANCE_CREATED.getValue() + "\" }");
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @GetMapping("/admin/characters")
  public ResponseEntity<?> getAllCharacters() {
    try {
      return ResponseEntity.status(HttpStatus.OK).body(charServ.getAllCharacters());
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @PostMapping("/profile/character")
  public ResponseEntity<String> updateCharacter(@RequestParam String quality) {
    try {
      User us = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
      if (us.getStats().getUpgradePoints() == 0)
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(
                "{\""
                    + ApiField.ERROR.getValue()
                    + "\":\""
                    + ApiMessage.NO_UPGRADE_POINTS.getValue()
                    + "\"}");
      GameCharacter ch = us.getCharacter();
      var parsedQuality = CharacterUpgradeQuality.fromRequest(quality);
      if (parsedQuality == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("{\"Quality " + quality + " doesn't exist.\"}");
      }
      switch (parsedQuality) {
        case DAMAGE:
          {
            ch.setPhysicalDamage(ch.getPhysicalDamage() + GameplayConstants.DAMAGE_UPGRADE_INCREMENT);
            break;
          }
        case HP:
          {
            ch.setMaxHP(ch.getMaxHp() + GameplayConstants.HP_UPGRADE_INCREMENT);
            break;
          }
        case RESISTANCE:
          {
            ch.setResistance(
                ch.getResistance()
                    + (1 - ch.getResistance()) / GameplayConstants.RESISTANCE_GROWTH_DIVISOR);
            break;
          }
        case CHAKRA:
          {
            ch.setMaxChakraAmount(ch.getMaxChakra() + GameplayConstants.CHAKRA_UPGRADE_INCREMENT);
            break;
          }
      }
      charServ.addCharacter(ch);
      Stats stats = us.getStats();
      stats.setUpgradePoints(stats.getUpgradePoints() - 1);
      statsServ.addStats(stats);
      userServ.saveUserWithoutBCrypt(us);
      return ResponseEntity.status(HttpStatus.OK)
          .body(
              "{\""
                  + ApiField.ANSWER.getValue()
                  + "\":\""
                  + ApiMessage.CHARACTER_UPDATED.getValue()
                  + "\"}");
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.getMessage());
    }
  }

  @GetMapping("/profile/character")
  public ResponseEntity<?> getCharacter() {
    try {
      User user =
          userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
      return ResponseEntity.status(HttpStatus.OK).body(user.getCharacter());
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
    }
  }

  @GetMapping("/users/{login}/character")
  public ResponseEntity<?> getCharacter(@PathVariable String login) {
    try {
      User user = userServ.getUser(login);
      if (user == null)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("{\"User " + login + " doesn't exist.\"}");
      return ResponseEntity.status(HttpStatus.OK).body(user.getCharacter());
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @PostMapping("/admin/users/{login}/grantAdmin")
  public ResponseEntity<String> grantAdmin(@PathVariable String login) {
    try {
      User user = userServ.getUser(login);
      if (user == null)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("{\"User " + login + " doesn't exist.\"}");
      Role admin = roleRep.findById("ADMIN").get();
      if (user.getRoles().contains(admin))
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body("{\"User " + login + " is already an administrator.\"}");
      user.addRole(admin);
      userServ.saveUserWithoutBCrypt(user);
      wsController.sendAdmin(login);
      return ResponseEntity.status(HttpStatus.OK)
          .body("{\"ADMIN role is granted for User " + user.getLogin() + ".\"}");
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @GetMapping("/users")
  public ResponseEntity<?> getAllUsers() {
    try {
      List users = userServ.getAllUsers();
      return ResponseEntity.status(HttpStatus.OK).body(users);
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @GetMapping("/users/admins")
  public ResponseEntity<?> getAdmins() {
    try {
      List<String> admins = new ArrayList<>();
      for (User user : userServ.getAllUsers()) {
        if (user.getRoles() != null
            && user.getRoles().stream().anyMatch((role) -> "ADMIN".equals(role.getRole()))) {
          admins.add(user.getLogin());
        }
      }
      return ResponseEntity.status(HttpStatus.OK).body(admins);
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @GetMapping("/users/{login}")
  public ResponseEntity<?> getUser(@PathVariable String login) {
    try {
      User user = userServ.getUser(login);
      if (user == null)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("{\"User " + login + " doesn't exist.\"}");
      return ResponseEntity.status(HttpStatus.OK).body(user);
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @DeleteMapping("/profile")
  public ResponseEntity<String> deleteUser() {
    try {
      String login = SecurityContextHolder.getContext().getAuthentication().getName();
      userServ.removeUser(login);
      return ResponseEntity.status(HttpStatus.OK).body("{\"User is deleted.\"}");
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error.getMessage());
    }
  }

  @GetMapping("/users/{login}/stats")
  public ResponseEntity<?> getStats(@PathVariable String login) {
    try {
      User user = userServ.getUser(login);
      if (user == null)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("{\"User " + login + " doesn't exist.\"}");
      Stats stats = user.getStats();
      return ResponseEntity.status(HttpStatus.OK).body(stats);
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @GetMapping("/friends")
  public ResponseEntity<?> getFriends() {
    try {
      User usr = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
      List<User> list = usr.getFriends();
      return ResponseEntity.status(HttpStatus.OK).body(list);
    } catch (Throwable error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
    }
  }

  @GetMapping("/ready")
  public ResponseEntity<?> getOnlineUsernames() {
    try {
      return ResponseEntity.status(HttpStatus.OK).body(onlinePresenceStore.listOnlineUsers());
    } catch (Throwable exc) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exc.getMessage());
    }
  }

  @GetMapping("/profile/online")
  public ResponseEntity<String> setOnline() {
    try {
      String username = SecurityContextHolder.getContext().getAuthentication().getName();
      onlinePresenceStore.markOnline(username);
      wsController.sendOnline(username + ":online");
      return ResponseEntity.status(HttpStatus.OK)
          .body(
              "{\""
                  + ApiField.RESPONSE.getValue()
                  + "\":\""
                  + ApiMessage.RESPONSE_OK.getValue()
                  + "\"}");
    } catch (Throwable exc) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exc.getMessage());
    }
  }

  @GetMapping("/profile/offline")
  public ResponseEntity<String> setOffline() {
    try {
      String username = SecurityContextHolder.getContext().getAuthentication().getName();
      onlinePresenceStore.markOffline(username);
      wsController.sendOnline(username + ":offline");
      return ResponseEntity.status(HttpStatus.OK)
          .body(
              "{\""
                  + ApiField.RESPONSE.getValue()
                  + "\":\""
                  + ApiMessage.RESPONSE_OK.getValue()
                  + "\"}");
    } catch (Throwable exc) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exc.getMessage());
    }
  }

  @GetMapping("/profile/pvphistory")
  public ResponseEntity<?> getPvpHistory() {
    ArrayList<PvpRecord> toRet = new ArrayList<>();
    User user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
    List<FightPVP> fights1 = user.getCharacter().getPvpFightsAsFirst();
    List<FightPVP> fights2 = user.getCharacter().getPvpFightsAsSecond();
    for (FightPVP fight : fights1) {
      PvpRecord record = new PvpRecord();
      record.setDate(fight.getFightDate());
      record.setRival(fight.getSecondFighter().getUser().getLogin());
      int rating = fight.isFirstWon() ? fight.getRatingChange() : -fight.getRatingChange();
      record.setResult(fight.isFirstWon() ? "Win" : "Loss");
      record.setRatingCh(rating);
      toRet.add(record);
    }
    for (FightPVP fight : fights2) {
      PvpRecord record = new PvpRecord();
      record.setDate(fight.getFightDate());
      record.setRival(fight.getFirstFighter().getUser().getLogin());
      int rating = fight.isFirstWon() ? -fight.getRatingChange() : fight.getRatingChange();
      record.setResult(fight.isFirstWon() ? "Loss" : "Win");
      record.setRatingCh(rating);
      toRet.add(record);
    }
    return ResponseEntity.ok(toRet);
  }

  @GetMapping("/profile/pvehistory")
  public ResponseEntity<?> getPveHistory() {
    var user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
    var character = user != null ? user.getCharacter() : null;
    if (character == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
    }
    ArrayList<PveRecord> toRet = new ArrayList<>();
    var fights = fightVsAIService.getByFighterId(character);
    for (AiFightParticipation fight : fights) {
      var record = new PveRecord();
      record.setDate(fight.getFight().getFight_date());
      record.setRival(fight.getFight().getBoss().getName());
      record.setXpCh(fight.getExperience());
      record.setResult(toHistoryResult(fight.getResult()));
      toRet.add(record);
    }
    return ResponseEntity.ok(toRet);
  }

  private String toHistoryResult(AiFightParticipation.Result result) {
    if (result == null) {
      return "Unknown";
    }
    return switch (result) {
      case WON -> "Win";
      case LOST -> "Loss";
      case DIED -> "Died";
    };
  }
}
