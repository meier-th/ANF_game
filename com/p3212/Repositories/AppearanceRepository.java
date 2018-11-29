package com.p3212.Repositories;

import com.p3212.EntityClasses.Appearance;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppearanceRepository extends CrudRepository<Appearance, Integer> {
}
