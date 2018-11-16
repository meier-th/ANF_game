package com.p3212.EntityClasses;

import java.io.Serializable;
import javax.persistence.Id;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;

/**
 * Represents Technique entity
 * Used to operate on spells
 */
@Entity
@Table(name = "Techniques")
public class Spell implements Serializable {

    /**
     * The base damage of a spell
     * negative damage = heal
     */
    private int baseDamage;

    /**
     * Identifier
     */
    @Id
    @GeneratedValue
    private int id;

    /**
     * additional damage acquired on each level
     */
    private int damagePerLevel;

    /**
     * The base chakra consumption of a spell
     */
    private int baseChakraConsumption;

    /**
     * additional chalra consumption acquired on each level
     */
    private int chakraConsumptionPerLevel;

    /**
     * Name of a spell
     */
    private String name;

    /**
     * Description of a spell
     */
    private String description;

    /**
     * Getter
     * {@link Spell#baseDamage}
     */
    public int getBaseDamage() {
        return baseDamage;
    }

    /**
     * Setter
     * {@link Spell#baseDamage}
     */
    public void setBaseDamage(int baseDamage) {
        this.baseDamage = baseDamage;
    }

    /**
     * Getter
     * {@link Spell#id}
     */
    public int getId() {
        return id;
    }

    /**
     * Setter
     * {@link Spell#id}
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Getter
     * {@link Spell#damagePerLevel}
     */
    public int getDamagePerLevel() {
        return damagePerLevel;
    }

    /**
     * Setter
     * {@link Spell#damagePerLevel}
     */
    public void setDamagePerLevel(int damagePerLevel) {
        this.damagePerLevel = damagePerLevel;
    }

    /**
     * Getter
     * {@link Spell#name}
     */
    public String getName() {
        return name;
    }

    /**
     * Setter
     * {@link Spell#name}
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter
     * {@link Spell#description}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter
     * {@link Spell#description}
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter
     * {@link Spell#baseChakraConsumption}
     */
    public int getBaseChakraConsumption() {
        return baseChakraConsumption;
    }

    /**
     * Setter
     * {@link Spell#baseChakraConsumption}
     */
    public void setBaseChakraConsumption(int baseChakraConsumption) {
        this.baseChakraConsumption = baseChakraConsumption;
    }

    /**
     * Getter
     * {@link Spell#chakraConsumptionPerLevel}
     */
    public int getChakraConsumptionPerLevel() {
        return chakraConsumptionPerLevel;
    }

    /**
     * Setter
     * {@link Spell#chakraConsumptionPerLevel}
     */
    public void setChakraConsumptionPerLevel(int chakraConsumptionPerLevel) {
        this.chakraConsumptionPerLevel = chakraConsumptionPerLevel;
    }

    /**
     * Default constructor
     * Used for dependency injection
     */
    public Spell() {
    }

    /**
     * To be used when retrieved from database
     *
     * @param name
     * @param description
     * @param baseDamage
     * @param lvldmg
     */
    public Spell(String name, String description, int baseDamage, int lvldmg) {
        this.name = name;
        this.baseDamage = baseDamage;
        this.description = description;
        this.damagePerLevel = lvldmg;
    }

    public Attack performAttack(int level, float resistance) {
        int damage = (int) ((baseDamage + level * damagePerLevel) * (1 - resistance));
        int chakra = baseChakraConsumption - level * chakraConsumptionPerLevel;
        return new Attack(damage, chakra);
    }

}
