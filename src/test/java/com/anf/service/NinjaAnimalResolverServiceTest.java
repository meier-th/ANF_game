package com.anf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anf.model.NinjaAnimal;
import com.anf.model.NinjaAnimalRace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NinjaAnimalResolverServiceTest {
  private NinjaAnimalService ninjaAnimalService;
  private NinjaAnimalResolverService resolverService;

  @BeforeEach
  void setUp() {
    ninjaAnimalService = mock(NinjaAnimalService.class);
    resolverService = new NinjaAnimalResolverService(ninjaAnimalService);
  }

  @Test
  void animalNameForRace_returnsExpectedMapping() {
    assertThat(resolverService.animalNameForRace(NinjaAnimalRace.Bugurt, true)).isEqualTo("Дядя Бафомет");
    assertThat(resolverService.animalNameForRace(NinjaAnimalRace.Veseliba, false)).isEqualTo("Vertet");
    assertThat(resolverService.animalNameForRace(NinjaAnimalRace.Lidzsvaru, true)).isEqualTo("Lapsa");
  }

  @Test
  void resolveByPvePvpAttackerToken_delegatesToNinjaAnimalService() {
    var animal = NinjaAnimal.animals.getFirst();
    when(ninjaAnimalService.findByName("Ubele")).thenReturn(animal);

    var resolved = resolverService.resolveByPvePvpAttackerToken("Ube");

    assertThat(resolved).isEqualTo(animal);
    verify(ninjaAnimalService).findByName("Ubele");
  }

  @Test
  void resolveByAnimalName_delegatesDirectLookup() {
    var animal = NinjaAnimal.animals.getLast();
    when(ninjaAnimalService.findByName("Erglis")).thenReturn(animal);

    var resolved = resolverService.resolveByAnimalName("Erglis");

    assertThat(resolved).isEqualTo(animal);
    verify(ninjaAnimalService).findByName("Erglis");
  }
}
