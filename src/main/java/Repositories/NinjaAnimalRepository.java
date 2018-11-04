package Repositories;

import EntityClasses.NinjaAnimal;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NinjaAnimalRepository extends CrudRepository<NinjaAnimal, Integer> {
}
