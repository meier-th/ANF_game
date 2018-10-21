package P3212.ANFBackend.DAOs;

import P3212.ANFBackend.EntityClasses.NinjaAnimal;
import P3212.ANFBackend.EntityClasses.NinjaAnimalRace;

import java.util.List;

/**
 * CRUD methods for NinjaAnimals
 */
public interface DAONinjaAnimalInterface {
    /**
     * Creates new ninja animal
     */
    void create(int id, String name, NinjaAnimalRace race, int damage, int hp, int reqlevel);

    /**
     * Updates the animal
     *
     * @param id       ID
     * @param damage   new Damage
     * @param hp       new hp
     * @param reqlevel new required level to summon
     */
    void update(int id, int damage, int hp, int reqlevel);

    /**
     * Returns the animal with the specified id
     *
     * @param id ID
     * @return needed NinjaAnimal if exists
     */
    NinjaAnimal get(int id);

    /**
     * Returns the list of all ninja animals
     *
     * @return list of {@link NinjaAnimal}
     */
    List<NinjaAnimal> list();
}
