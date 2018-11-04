package Repositories;

import org.springframework.data.repository.CrudRepository;
import EntityClasses.Character;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterRepository extends CrudRepository<Character, Integer>{
    
}
