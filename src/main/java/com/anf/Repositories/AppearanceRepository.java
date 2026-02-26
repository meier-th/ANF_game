package com.anf.Repositories;

import com.anf.EntityClasses.Appearance;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppearanceRepository extends CrudRepository<Appearance, Integer> {
}
