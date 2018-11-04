package Services;

import EntityClasses.NinjaAnimal;
import Repositories.NinjaAnimalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for Ninja Animal Entity
 */

@Service
public class NinjaAnimalService {

    /**
     * Entity repository
     */
    @Autowired
    NinjaAnimalRepository repository;

    /**
     * Adds new animal to database
     *
     * @param animal A new one to add
     */

    void addAnimal(NinjaAnimal animal) {
        repository.save(animal);
    }

    /**
     * Get an animal with the id
     *
     * @param id id of animal
     * @return Requested NinjaAnimal
     */

    NinjaAnimal get(int id) {
        return repository.findById(id).get();
    }

    /**
     * Get all NinjaAnimals
     *
     * @return Iterable with all NinjaAnimals
     */
    Iterable<NinjaAnimal> list() {
        return repository.findAll();
    }

    /**
     * Removes animal with the id
     *
     * @param id Id to be deleted
     */
    void removeAnimal(int id) {
        repository.deleteById(id);
    }

    /**
     * Updates an animal
     *
     * @param id     animal id
     * @param animal NinjaAnimal object to be saved in db
     */
    void updateAnimal(int id, NinjaAnimal animal) {
        repository.save(animal); //TODO do we really need save?
    }

}
