package com.anf.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.anf.model.database.Spell;

@Repository
public interface SpellRepository extends CrudRepository<Spell, String> {}
