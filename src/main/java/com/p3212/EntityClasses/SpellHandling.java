package com.p3212.EntityClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;

/**
 * Represents USERS_OF_TECHNIQUES entity
 * Used to operate on character's spells abilities
 */
@Entity
public class SpellHandling {


    @Id
    private int handlingId;

    /**
     * Character
     */
    @ManyToOne
    @JoinColumn(name = "character")
    @JsonIgnore
    private Character characterHandler;

    /**
     * Spell
     */

    @ManyToOne
    @JoinColumn(name = "spell")
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
    public SpellHandling() {
    }

    /**
     * To be used when retrieved from database
     *
     * @param level
     */
    public SpellHandling(int level) {
        this.spellLevel = level;
    }

}
