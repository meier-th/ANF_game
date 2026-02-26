package com.anf.Repositories;

import com.anf.EntityClasses.Spell;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpellRepository extends CrudRepository<Spell, String> {
}
