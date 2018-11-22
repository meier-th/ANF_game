package com.p3212.Repositories;

import com.p3212.EntityClasses.SpellHandling;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpellHandlingRepository extends CrudRepository<SpellHandling, Integer> {
    @Query("select s from SpellHandling s where s.characterHandler = :id")
    List<SpellHandling> getCharactersHandlings(@Param("id") int id);
}
