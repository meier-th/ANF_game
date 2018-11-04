package Repositories;


import EntityClasses.NinjaAnimalRace;
import org.springframework.data.repository.Repository;

@org.springframework.stereotype.Repository
public interface NinjaAnimalRaceRepository extends Repository<NinjaAnimalRace, Integer> {

    NinjaAnimalRace save(NinjaAnimalRace race);

    NinjaAnimalRace findById(int id);
}
