package Services;

import EntityClasses.NinjaAnimalRace;
import Repositories.NinjaAnimalRaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NinjaAnimalRaceService {

    @Autowired
    NinjaAnimalRaceRepository repository;

    void addRace(NinjaAnimalRace race) {
        repository.save(race);
    }

    NinjaAnimalRace getRace(int id) {
        return repository.findById(id);
    }
}
