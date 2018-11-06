package Repositories;

import EntityClasses.SpellHandling;
import EntityClasses.SpellHandlingCompositeKey;
import org.springframework.data.repository.CrudRepository;

public interface SpellHandlingRepository extends CrudRepository<SpellHandling, SpellHandlingCompositeKey> {
    
}
