package com.anf.repository;

import com.anf.model.Spell;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpellRepository extends CrudRepository<Spell, String> {}
