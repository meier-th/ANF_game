package com.p3212.Repositories;

import com.p3212.EntityClasses.SpellHandling;
import com.p3212.EntityClasses.SpellHandlingCompositeKey;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SpellHandlingRepository extends CrudRepository<SpellHandling, SpellHandlingCompositeKey> {
    @Query("select s from SpellHandling s where s.handlingId.characterHandler = :id")
    List<SpellHandling>getCharactersHandlings(@Param("id") int id);
}
