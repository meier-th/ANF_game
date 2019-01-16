package com.p3212.Repositories;

import com.p3212.EntityClasses.Spell;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpellRepository extends CrudRepository<Spell, String> {
}
