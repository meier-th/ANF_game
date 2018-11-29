package com.p3212.Repositories;

import com.p3212.EntityClasses.NinjaAnimalRace;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NinjaAnimalRaceRepository extends CrudRepository<NinjaAnimalRace, NinjaAnimalRace.races> {
    
}
