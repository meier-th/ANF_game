package com.anf.domain.combat;

import com.anf.model.database.GameCharacter;
import com.anf.model.database.Spell;
import com.anf.model.database.SpellKnowledge;
import com.anf.infrastructure.persistence.repository.SpellKnowledgeRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SpellKnowledgeService {

  private final SpellKnowledgeRepository repository;

  public void addOrUpdateHandling(SpellKnowledge sh) {
    repository.save(sh);
  }

  public List<SpellKnowledge> getPersonsHandling(GameCharacter ch) {
    return repository.getCharactersHandlings(ch);
  }

  public SpellKnowledge getSpellKnowledge(GameCharacter character, Spell spell) {
    return repository.findByCharacterHandlerAndSpellUse(character, spell).orElse(null);
  }
}
