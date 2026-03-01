package com.anf.repository;

import com.anf.model.Appearance;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppearanceRepository extends CrudRepository<Appearance, Integer> {}
