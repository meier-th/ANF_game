package com.anf.service;

import com.anf.model.database.Spell;
import com.anf.repository.SpellRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SpellService {
  private final SpellRepository repository;

  public Iterable<Spell> getAllSpells() {
    return repository.findAll();
  }

  public Spell get(String name) {
    return repository.findById(name).orElse(null);
  }
}
