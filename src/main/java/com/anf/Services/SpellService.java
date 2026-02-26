package com.anf.Services;

import com.anf.EntityClasses.Spell;
import com.anf.Repositories.SpellRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpellService {
    @Autowired
    SpellRepository repository;

    public Iterable<Spell> getAllSpells() {
        return repository.findAll();
    }

    public Spell get (String name) {
        return repository.findById(name).orElse(null);
    }

}

