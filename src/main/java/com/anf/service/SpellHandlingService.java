package com.anf.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.anf.model.GameCharacter;
import com.anf.model.Spell;
import com.anf.model.SpellHandling;
import com.anf.repository.SpellHandlingRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SpellHandlingService {

    private final SpellHandlingRepository repository;

    public void addOrUpdateHandling(SpellHandling sh) {
        repository.save(sh);
    }

    public List<SpellHandling> getPersonsHandling(GameCharacter ch) {
        return repository.getCharactersHandlings(ch);
    }

    public SpellHandling getSpellHandling(GameCharacter character, Spell spell) {
        return repository.findByCharacterHandlerAndSpellUse(character, spell).orElse(null);
    }
}
