package EntityClasses;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;

/**
 * Represents USERS_OF_TECHNIQUES entity
 * Used to operate on character's spells abilities
 * 
 */
@Entity
public class SpellHandling {
    
    /**
     * Character
     */
    private Character character;
    
    /**
     * Spell
     */
    private Spell spell;
    
    /**
     * Level of the spell
     */
    @Id
    private int spellLevel;

    /**
     * Contains all information about characters' spells knowledge
     */
    public static ArrayList<SpellHandling> infoAboutSpells;
    
    /**
     * Getter
     * {@link SpellHandling#character} 
     */
    public Character getCharacter() {
        return character;
    }

    /**
     * Setter
     * {@link SpellHandling#character} 
     */
    public void setCharacter(Character character) {
        this.character = character;
    }

    /**
     * Getter
     * {@link SpellHandling#spell} 
     */
    public Spell getSpell() {
        return spell;
    }

    /**
     * Setter
     * {@link SpellHandling#spell} 
     */
    public void setSpell(Spell spell) {
        this.spell = spell;
    }

    /**
     * Getter
     * {@link SpellHandling#spellLevel} 
     */
    public int getSpellLevel() {
        return spellLevel;
    }

    /**
     * Setter
     * {@link SpellHandling#spellLevel} 
     */
    public void setSpellLevel(int spellLevel) {
        this.spellLevel = spellLevel;
    }
    
    /**
     * Default constructor
     * Used for dependency injection
     */
    public SpellHandling(){}
    
    /**
     * To be used when retrieved from database
     * @param character
     * @param spell
     * @param level 
     */
    public SpellHandling(Character character, Spell spell, int level){ 
        this.character = character;
        this.spell = spell;
        this.spellLevel = level;
    }
    
}
