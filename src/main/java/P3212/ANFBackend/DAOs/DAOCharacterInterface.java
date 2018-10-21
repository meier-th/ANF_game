package P3212.ANFBackend.DAOs;

import P3212.ANFBackend.EntityClasses.Character;
import P3212.ANFBackend.EntityClasses.NinjaAnimalRace;

import java.time.LocalDate;
import java.util.List;

/**
 * CRUD interface for {@link Character}
 */
public interface DAOCharacterInterface {
    /**
     * Creates a new character with specified characteristics
     *
     * @param date       Date of character creation
     * @param resistance Character's resistance to attacks. Initially is 0
     * @param hp         Character's hp.
     * @param damage     Character's damage
     * @param chakra     Character's chakra
     * @param race       Character's animal race to summon
     */
    void create(int id, LocalDate date, float resistance, int hp, int damage, int chakra, NinjaAnimalRace race);

    /**
     * Deletes the character with this ID
     *
     * @param id ID to be deleted
     */
    void delete(int id);

    /**
     * Updates character's characteristics
     *
     * @param resistance New level of resistance
     * @param hp         New level of HP
     * @param damage     New level of damage
     * @param chakra     New level of chakra
     */
    void update(float resistance, int hp, int damage, int chakra);

    /**
     * Returns the character with specified ID
     *
     * @param id ID of needed character
     * @return
     */
    Character get(int id);

    /**
     * Returns the list of all characters
     *
     * @return
     */
    List<Character> list();


}
