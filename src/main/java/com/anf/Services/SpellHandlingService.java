package com.anf.Services;

import com.anf.EntityClasses.Spell;
import com.anf.EntityClasses.SpellHandling;
import com.anf.EntityClasses.Character;
import com.anf.Repositories.SpellHandlingRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpellHandlingService {

    @Autowired
    SpellHandlingRepository repository;

    public void addOrUpdateHandling(SpellHandling sh) {
        repository.save(sh);
    }

    public List<SpellHandling> getPersonsHandling(Character ch) {
        return repository.getCharactersHandlings(ch);
    }

    public SpellHandling getSpellHandling(Character character, Spell spell) {
        return repository.findByCharacterHandlerAndSpellUse(character, spell).orElse(null);
    }
}
