package com.p3212.Repositories;

import org.springframework.data.repository.CrudRepository;
import com.p3212.EntityClasses.Character;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterRepository extends CrudRepository<Character, Integer>{
    
}
