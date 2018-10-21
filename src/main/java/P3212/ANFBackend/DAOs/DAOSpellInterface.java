package P3212.ANFBackend.DAOs;

import P3212.ANFBackend.EntityClasses.Spell;
import java.util.List;

/**
 * CRUD interface for 'Spells'
 * Uses a mapper - implementation of a RowMapper<> interface for ORM purpose 
 */
public interface DAOSpellInterface {
    
    /**
     * Gets a Spell object from database by its id.
     * @param id - the id of a Spell
     * @return A spell object
     */
    public Spell getSpell(int id);
    
    /**
     *  Gets a Spell object from database by its name
     * @param name - the name of a Spell
     * @return A spell object
     */
    public Spell getSpell(String name);
    
    /**
     *  Gets all Spells from database
     * To be used on application start-up
     * @return A list of Spells
     */
    public List<Spell> listSpells();
    
}
