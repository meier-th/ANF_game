package com.p3212.EntityClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Represents Person entity
 * Used to operate on in-game character's properties
 */
@Entity
@Table(name = "persons")
public class Character extends Creature implements Serializable {

    /**
     * The date of creating a character
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @ManyToOne
    @JoinColumn(name = "animal_race")
    private NinjaAnimalRace animalRace;

    @OneToMany(mappedBy="fighter")
    private List<UserAIFight> fights;
    
    
    public NinjaAnimalRace getAnimalRace() {
        return animalRace;
    }

    public void setAnimalRace(NinjaAnimalRace animalRace) {
        this.animalRace = animalRace;
    }

    @OneToMany(mappedBy = "handlingId.characterHandler")
    private List<SpellHandling> spellsKnown;

    public List<SpellHandling> getSpellsKnown() {
        return spellsKnown;
    }

    
    
    public void setSpellsKnown(List<SpellHandling> spellsKnown) {
        this.spellsKnown = spellsKnown;
    }

    /**
     * Identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    /**
     * The maximum amount of chakra (mana)
     */
    private int maxChakraAmount;

    @OneToMany(mappedBy = "pvpId.firstFighter")
    @JsonIgnore
    private List<FightPVP> pvpFightsAsFirst;

    @OneToMany(mappedBy = "pvpId.secondFighter")
    @JsonIgnore
    private List<FightPVP> pvpFightsAsSecond;

    @OneToOne(mappedBy = "character")
    @JsonIgnore
    private User user;

    public List<UserAIFight> getFights() {
        return fights;
    }

    public void setFights(List<UserAIFight> fights) {
        this.fights = fights;
    }

    /**
     * Appearance object for this character
     */
    @OneToOne
    @JoinColumn(name = "appearance_id")
    private Appearance appearance;

    public Appearance getAppearance() {
        return appearance;
    }

    public void setAppearance(Appearance appearance) {
        this.appearance = appearance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * The maximum amount of HP
     */
    private int maxHP;

    /**
     * The damage of a physical attack
     */
    private int physicalDamage;

    /**
     * The portion of income damage to be blocked.
     * 0 <= resistance < 0.5
     * next level: resistance = resistance + (1-resistance)/4
     */
    private float resistance;

    /**
     * Getter
     * {@link Character#creationDate}
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Setter
     * {@link Character#creationDate}
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Setter
     * {@link Character#maxChakraAmount}
     */
    public void setMaxChakraAmount(int maxChakraAmount) {
        this.maxChakraAmount = maxChakraAmount;
    }

    /**
     * Getter
     * {@link Character#id}
     */
    public int getId() {
        return id;
    }

    /**
     * Setter
     * {@link Character#id}
     */
    public void setId(int id) {
        this.id = id;
    }

    public List<FightPVP> getPvpFightsAsFirst() {
        return pvpFightsAsFirst;
    }

    public void setPvpFightsAsFirst(List<FightPVP> pvpFightsAsFirst) {
        this.pvpFightsAsFirst = pvpFightsAsFirst;
    }

    public List<FightPVP> getPvpFightsAsSecond() {
        return pvpFightsAsSecond;
    }

    public void setPvpFightsAsSecond(List<FightPVP> pvpFightsAsSecond) {
        this.pvpFightsAsSecond = pvpFightsAsSecond;
    }


    /**
     * Setter
     * {@link Character#maxHP}
     */
    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
    }

    /**
     * Getter
     * {@link Character#physicalDamage}
     */
    public int getPhysicalDamage() {
        return physicalDamage;
    }

    /**
     * Setter
     * {@link Character#physicalDamage}
     */
    public void setPhysicalDamage(int physicalDamage) {
        this.physicalDamage = physicalDamage;
    }

    /**
     * Setter
     * {@link Character#resistance}
     */
    public void setResistance(float resistance) {
        this.resistance = resistance;
    }

    /**
     * Default constructor. To be used for dependency injection
     */
    public Character() {
    }

    /**
     * @param date       - creation date
     * @param resistance - resistance float value
     * @param hp         - max hp value
     * @param damage     - physical damahe value
     * @param chakra     - amount of chakra
     */
    public Character(Date date, float resistance, int hp, int damage, int chakra) {
        this.creationDate = date;
        this.resistance = resistance;
        this.maxHP = hp;
        this.physicalDamage = damage;
        this.maxChakraAmount = chakra;
    }

    public Character(float resistance, int hp, int damage, int chakra) {
        this.creationDate = new Date();
        this.resistance = resistance;
        this.maxHP = hp;
        this.physicalDamage = damage;
        this.maxChakraAmount = chakra;
    }
    
    @Override
    public void acceptDamage(int damage) {
        currentHP -= damage;
    }

    /**
     * Getter
     * {@link Character#resistance}
     */
    @Override
    public float getResistance() {
        return resistance;
    }

    @Override
    public int getLevel() {
        return user.getStats().getLevel();
    }

    @Override
    public int getMaxHp() {
        return maxHP;
    }

    @Override
    public int getMaxChakra() {
        return maxChakraAmount;
    }
}

