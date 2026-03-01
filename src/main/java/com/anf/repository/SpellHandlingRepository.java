package com.anf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anf.model.GameCharacter;
import com.anf.model.Spell;
import com.anf.model.SpellHandling;

@Repository
public interface SpellHandlingRepository extends CrudRepository<SpellHandling, Integer> {
    @Query("select s from SpellHandling s where s.characterHandler = :ch")
    List<SpellHandling> getCharactersHandlings(@Param("ch") GameCharacter ch);

    Optional<SpellHandling> findByCharacterHandlerAndSpellUse(GameCharacter character, Spell spell);
}
