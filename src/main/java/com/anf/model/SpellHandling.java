package com.anf.model;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents USERS_OF_TECHNIQUES entity
 * Used to operate on character's spells abilities
 */
@Entity
public class SpellHandling {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int handlingId;

    /**
     * Character
     */
    @ManyToOne
    @JoinColumn(name = "character", nullable=false)
    @JsonIgnore
    private GameCharacter characterHandler;

    /**
     * Spell
     */

    @ManyToOne
    @JoinColumn(name = "spell", nullable=false)
    private Spell spellUse;

    /**
     * Level of the spell
     */
    @Column(nullable=false)
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

    public SpellHandling() {
    }

    public SpellHandling(int level, Spell spell, GameCharacter user) {
        this.spellLevel = level;
        this.spellUse = spell;
        this.characterHandler = user;
    }

    public int getHandlingId() {
        return handlingId;
    }

    public void setHandlingId(int handlingId) {
        this.handlingId = handlingId;
    }

    public GameCharacter getCharacterHandler() {
        return characterHandler;
    }

    public void setCharacterHandler(GameCharacter characterHandler) {
        this.characterHandler = characterHandler;
    }

    public Spell getSpellUse() {
        return spellUse;
    }

    public void setSpellUse(Spell spellUse) {
        this.spellUse = spellUse;
    }

    public static ArrayList<SpellHandling> getInfoAboutSpells() {
        return infoAboutSpells;
    }

    public static void setInfoAboutSpells(ArrayList<SpellHandling> infoAboutSpells) {
        SpellHandling.infoAboutSpells = infoAboutSpells;
    }

    
    
}
