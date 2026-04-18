package com.anf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.anf.model.database.Boss;
import com.anf.model.database.GameCharacter;
import com.anf.model.database.Stats;
import com.anf.model.database.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FightRuntimeFactoryServiceTest {
  private UserService userService;
  private BossService bossService;
  private FightRuntimeFactoryService fightRuntimeFactoryService;

  @BeforeEach
  void setUp() {
    userService = mock(UserService.class);
    bossService = mock(BossService.class);
    fightRuntimeFactoryService = new FightRuntimeFactoryService(userService, bossService);
  }

  @Test
  void createPvpRuntimeFight_returnsNull_whenParticipantsCountIsInvalid() {
    var result = fightRuntimeFactoryService.createPvpRuntimeFight(List.of("alice"));
    assertThat(result).isNull();
  }

  @Test
  void createPvpRuntimeFight_buildsFightAndRatingChanges() {
    var alice = userWithCharacter("alice", 200);
    var bob = userWithCharacter("bob", 160);
    when(userService.getUser("alice")).thenReturn(alice);
    when(userService.getUser("bob")).thenReturn(bob);

    var result = fightRuntimeFactoryService.createPvpRuntimeFight(List.of("alice", "bob"));

    assertThat(result).isNotNull();
    assertThat(result.getFighter1().getLogin()).isEqualTo("alice");
    assertThat(result.getFighter2().getLogin()).isEqualTo("bob");
    assertThat(result.getBiggerRatingChange()).isEqualTo(25);
    assertThat(result.getLessRatingChange()).isEqualTo(10);
  }

  @Test
  void createPveRuntimeFight_returnsNull_whenBossIsMissing() {
    when(bossService.getBossByName("unknown")).thenReturn(null);
    var result = fightRuntimeFactoryService.createPveRuntimeFight(List.of("alice"), "unknown");
    assertThat(result).isNull();
  }

  @Test
  void createPveRuntimeFight_buildsFightWithParticipants() {
    var boss = mock(Boss.class);
    when(bossService.getBossByName("Shukaku")).thenReturn(boss);
    var alice = userWithCharacter("alice", 100);
    var bob = userWithCharacter("bob", 130);
    when(userService.getUser("alice")).thenReturn(alice);
    when(userService.getUser("bob")).thenReturn(bob);

    var result = fightRuntimeFactoryService.createPveRuntimeFight(List.of("alice", "bob"), "Shukaku");

    assertThat(result).isNotNull();
    assertThat(result.getFighters()).hasSize(2);
    assertThat(result.getSetFighters()).hasSize(2);
    assertThat(result.getBoss()).isEqualTo(boss);
  }

  private User userWithCharacter(String login, int rating) {
    var user = new User();
    user.setLogin(login);
    user.setStats(new Stats(rating, 0, 0, 0, 0, 0, 1, 3));
    var character = new GameCharacter(0.1f, 100, 10, 30);
    character.setUser(user);
    user.setCharacter(character);
    return user;
  }
}
