package EntityClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import java.util.ArrayList;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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
    
    @Id
    @GeneratedValue
    private int id;
    
    
    @ManyToOne
    @JoinColumn(name="character")
    @JsonIgnore
    private Character characterHandler;
    
    /**
     * Spell
     */
    
    @ManyToOne
    @JoinColumn(name="spell")
    private Spell spellUse;
    
    /**
     * Level of the spell
     */
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
        return characterHandler;
    }

    /**
     * Setter
     * {@link SpellHandling#character} 
     */
    public void setCharacter(Character character) {
        this.characterHandler = character;
    }

    /**
     * Getter
     * {@link SpellHandling#spell} 
     */
    public Spell getSpell() {
        return spellUse;
    }

    /**
     * Setter
     * {@link SpellHandling#spell} 
     */
    public void setSpell(Spell spell) {
        this.spellUse = spell;
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
        this.characterHandler = character;
        this.spellUse = spell;
        this.spellLevel = level;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Character getCharacterHandler() {
        return characterHandler;
    }

    public void setCharacterHandler(Character characterHandler) {
        this.characterHandler = characterHandler;
    }

    public Spell getSpellUse() {
        return spellUse;
    }

    public void setSpellUse(Spell spellUse) {
        this.spellUse = spellUse;
    }
    
}
