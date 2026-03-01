package com.anf.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.anf.model.GameCharacter;

@Repository
public interface CharacterRepository extends CrudRepository<GameCharacter, Integer>{
    
}
