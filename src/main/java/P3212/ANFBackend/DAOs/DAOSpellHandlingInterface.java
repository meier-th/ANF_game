package P3212.ANFBackend.DAOs;

import P3212.ANFBackend.EntityClasses.Spell;
import P3212.ANFBackend.EntityClasses.SpellHandling;
import P3212.ANFBackend.EntityClasses.Character;
import java.util.List;

/**
 * CRUD interface for 'SpellsHandling'
 * Uses a mapper - implementation of a RowMapper<> interface for ORM purpose 
 */
public interface DAOSpellHandlingInterface {
    
    /**
     * Returns a list of SpellHandling of a specific Character
     * @param character - character
     * @return a List of SpellHandling
     */
    public List<SpellHandling> getByCharacter(Character character);
    
    /**
     * Returns a specific SpellHandling
     * @param character - character
     * @param spell - Spell
     * @return  a SpellHandling object
     */
    public SpellHandling getSpellHandlingOfCharacter(Character character, Spell spell);
    
    /**
     * Returns all SpellHandling objects
     * @return a List of SpellHandling objects
     */
    public List<SpellHandling> getAllSpellHandling();
    
    /**
     * Creates a new SpellHandling record in database
     * Level of spell is set to 1
     * @param charater - Character
     * @param spell - Spell
     */
    public void createSpellHandling(Character charater, Spell spell);
    
    /**
     * Removes a SpellHandling record from database
     * @param character - Character
     * @param spell - Spell
     */
    public void deleteSpellHandling(Character character, Spell spell);
    
    /**
     * Updates a SpellHandling record in database
     * Use case: Character acquires a new level of a spell
     * @param character - Character
     * @param spell - Spell
     * @param level - new level of a Spell
     */
    public void updateSpellHandling(Character character, Spell spell, int level);
    
}
