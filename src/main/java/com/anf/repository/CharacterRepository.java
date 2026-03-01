package com.anf.repository;

import com.anf.model.GameCharacter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterRepository extends CrudRepository<GameCharacter, Integer> {}
