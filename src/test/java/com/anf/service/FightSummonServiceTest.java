package com.anf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.config.WebSocketsController;
import com.anf.model.NinjaAnimal;
import com.anf.model.NinjaAnimalRace;
import com.anf.model.database.FightPVP;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Stats;
import com.anf.model.database.User;
import com.anf.service.state.LegacyFightRuntimeStore;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class FightSummonServiceTest {
  private UserService userService;
  private NinjaAnimalResolverService ninjaAnimalResolverService;
  private LegacyFightRuntimeStore fightStateStore;
  private WebSocketsController webSocketsController;
  private FightSummonService fightSummonService;

  @BeforeEach
  void setUp() {
    userService = mock(UserService.class);
    ninjaAnimalResolverService = mock(NinjaAnimalResolverService.class);
    fightStateStore = mock(LegacyFightRuntimeStore.class);
    webSocketsController = mock(WebSocketsController.class);
    fightSummonService =
        new FightSummonService(
            userService, ninjaAnimalResolverService, fightStateStore, webSocketsController);
  }

  @Test
  void summonPvp_returnsNotFound_whenFightMissing() {
    when(fightStateStore.getFight("fight-1")).thenReturn(Optional.empty());

    var response = fightSummonService.summonPvp("fight-1", "alice");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void summonPvp_returnsForbidden_whenUserHasNoAnimalRace() {
    var user = userWithRace("alice", null);
    when(userService.getUser("alice")).thenReturn(user);
    var fight = new FightPVP();
    fight.setFighters(userWithRace("alice", null).getCharacter(), userWithRace("bob", NinjaAnimalRace.Bugurt).getCharacter());
    when(fightStateStore.getFight("fight-1")).thenReturn(Optional.of(fight));

    var response = fightSummonService.summonPvp("fight-1", "alice");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    verify(ninjaAnimalResolverService, never()).resolveByAnimalName(any());
  }

  @Test
  void summonPvp_addsAnimalAndSavesFight() {
    var alice = userWithRace("alice", NinjaAnimalRace.Bugurt);
    var bob = userWithRace("bob", NinjaAnimalRace.Veseliba);
    when(userService.getUser("alice")).thenReturn(alice);
    var fight = new FightPVP();
    fight.setFighters(alice.getCharacter(), bob.getCharacter());
    when(fightStateStore.getFight("fight-1")).thenReturn(Optional.of(fight));
    var animal = NinjaAnimal.animals.get(0);
    when(ninjaAnimalResolverService.animalNameForRace(NinjaAnimalRace.Bugurt, true))
        .thenReturn("Дядя Бафомет");
    when(ninjaAnimalResolverService.resolveByAnimalName("Дядя Бафомет")).thenReturn(animal);

    var response = fightSummonService.summonPvp("fight-1", "alice");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(fight.getAnimals1()).hasSize(1);
    verify(fightStateStore).saveFight("fight-1", fight);
    verify(webSocketsController).sendSummon("bob", "alice", animal, "Дядя Бафомет");
  }

  @Test
  void summonPve_returnsNotFound_whenFightMissing() {
    when(userService.getUser("alice")).thenReturn(userWithRace("alice", NinjaAnimalRace.Bugurt));
    when(fightStateStore.getFight("fight-2")).thenReturn(Optional.empty());

    var response = fightSummonService.summonPve("fight-2", "alice");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  private User userWithRace(String login, NinjaAnimalRace race) {
    var user = new User();
    user.setLogin(login);
    user.setStats(new Stats(100, 0, 0, 0, 0, 0, 10, 3));
    var character = new GameCharacter(0.1f, 100, 10, 30);
    character.setAnimalRace(race);
    character.setUser(user);
    user.setCharacter(character);
    return user;
  }
}
