package com.anf.Repositories;

import org.springframework.data.repository.CrudRepository;
import com.anf.EntityClasses.Character;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterRepository extends CrudRepository<Character, Integer>{
    
}
