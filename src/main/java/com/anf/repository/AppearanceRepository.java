package com.anf.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.anf.model.Appearance;

@Repository
public interface AppearanceRepository extends CrudRepository<Appearance, Integer> {
}
