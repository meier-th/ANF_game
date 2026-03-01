package com.anf.repository;

import com.anf.model.database.CharacterAppearance;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppearanceRepository extends CrudRepository<CharacterAppearance, Integer> {}
