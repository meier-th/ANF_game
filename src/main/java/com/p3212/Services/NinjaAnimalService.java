package com.p3212.Services;

import com.google.common.collect.Lists;
import com.p3212.EntityClasses.NinjaAnimal;
import com.p3212.EntityClasses.NinjaAnimalRace;
import com.p3212.Repositories.NinjaAnimalRepository;
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
     * @param race race to get
     */

    public List<NinjaAnimal> getRaceAnimals(NinjaAnimalRace race) {
        
        return repository.getRaceAnimals(race);
    }

    public void addAnimal(NinjaAnimal animal) {
        repository.save(animal);
    }

    /**
     * Get an animal with the id
     *
     * @param id id of animal
     * @return Requested NinjaAnimal
     */

    public NinjaAnimal get(String id) {
        return repository.findById(id).get();
    }

    /**
     * Get all NinjaAnimals
     *
     * @return Iterable with all NinjaAnimals
     */
    public List<NinjaAnimal> list() {
        return Lists.newArrayList(repository.findAll());
    }

    /**
     * Removes animal with the id
     *
     * @param id Id to be deleted
     */
    public void removeAnimal(String id) {
        repository.deleteById(id);
    }

    /**
     * Updates an animal
     *
     * @param id     animal id
     * @param animal NinjaAnimal object to be saved in db
     */
    public void updateAnimal(int id, NinjaAnimal animal) {
        repository.save(animal);
    }

}
