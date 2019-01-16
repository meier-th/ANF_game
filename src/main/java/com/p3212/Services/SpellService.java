package com.p3212.Services;

import com.p3212.EntityClasses.Spell;
import com.p3212.Repositories.SpellRepository;
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

