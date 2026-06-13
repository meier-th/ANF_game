package com.anf.domain.combat;

import com.anf.model.database.GameCharacter;
import com.anf.model.database.Spell;
import com.anf.model.database.SpellKnowledge;
import com.anf.infrastructure.persistence.repository.SpellKnowledgeRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SpellKnowledgeService {

  private final SpellKnowledgeRepository repository;
  private final SpellService spellService;

  public void addOrUpdateHandling(SpellKnowledge sh) {
    repository.save(sh);
  }

  public List<SpellKnowledge> getPersonsHandling(GameCharacter ch) {
    return repository.getCharactersHandlings(ch);
  }

  public SpellKnowledge getSpellKnowledge(GameCharacter character, Spell spell) {
    return repository.findByCharacterHandlerAndSpellUse(character, spell).orElse(null);
  }

  public List<SpellKnowledge> ensureUnlockedSpellKnowledge(GameCharacter character) {
    if (character == null || character.getUser() == null || character.getUser().getStats() == null) {
      return List.of();
    }
    int characterLevel = character.getUser().getStats().getLevel();
    List<SpellKnowledge> existingKnowledges = repository.getCharactersHandlings(character);
    Map<String, SpellKnowledge> knowledgesBySpell = new HashMap<>();
    for (SpellKnowledge knowledge : existingKnowledges) {
      if (knowledge.getSpellUse() != null && knowledge.getSpellUse().getName() != null) {
        knowledgesBySpell.put(knowledge.getSpellUse().getName(), knowledge);
      }
    }

    for (Spell spell : spellService.getAllSpells()) {
      if (spell == null || spell.getName() == null || characterLevel < spell.getReqLevel()) {
        continue;
      }
      SpellKnowledge knownSpell = knowledgesBySpell.get(spell.getName());
      if (knownSpell == null) {
        SpellKnowledge unlockedSpell = new SpellKnowledge(1, spell, character);
        repository.save(unlockedSpell);
        knowledgesBySpell.put(spell.getName(), unlockedSpell);
      } else if (knownSpell.getSpellLevel() < 1) {
        knownSpell.setSpellLevel(1);
        repository.save(knownSpell);
      }
    }

    List<SpellKnowledge> result = repository.getCharactersHandlings(character);
    result.sort(
        Comparator.comparingInt(
            (SpellKnowledge knowledge) ->
                knowledge.getSpellUse() == null ? Integer.MAX_VALUE : knowledge.getSpellUse().getReqLevel()));
    return result;
  }
}
