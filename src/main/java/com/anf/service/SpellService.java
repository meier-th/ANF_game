package com.anf.service;

import org.springframework.stereotype.Service;

import com.anf.model.Spell;
import com.anf.repository.SpellRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SpellService {
    private final SpellRepository repository;

    public Iterable<Spell> getAllSpells() {
        return repository.findAll();
    }

    public Spell get (String name) {
        return repository.findById(name).orElse(null);
    }

}

