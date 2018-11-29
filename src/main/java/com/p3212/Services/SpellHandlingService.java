package com.p3212.Services;

import com.p3212.EntityClasses.Spell;
import com.p3212.EntityClasses.SpellHandling;
import com.p3212.EntityClasses.Character;
import com.p3212.Repositories.SpellHandlingRepository;

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
        return repository.findByCharacterHandlerAndSpellUse(character, spell).get();
    }
}
