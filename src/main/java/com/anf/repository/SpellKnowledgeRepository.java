package com.anf.repository;

import com.anf.model.database.GameCharacter;
import com.anf.model.database.Spell;
import com.anf.model.database.SpellKnowledge;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpellKnowledgeRepository extends CrudRepository<SpellKnowledge, Integer> {
  @Query("select s from SpellKnowledge s where s.characterHandler = :ch")
  List<SpellKnowledge> getCharactersHandlings(@Param("ch") GameCharacter ch);

  Optional<SpellKnowledge> findByCharacterHandlerAndSpellUse(GameCharacter character, Spell spell);
}
