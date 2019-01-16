package com.p3212.EntityClasses;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Id;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
    @Column(nullable=false)
    private int baseDamage;

    /**
     * additional damage acquired on each level
     */
    @Column(nullable=false)
    private int damagePerLevel;

    /**
     * The base chakra consumption of a spell
     */
    @Column(nullable=false)
    private int baseChakraConsumption;

    /**
     * additional chalra consumption acquired on each level
     */
    @Column(nullable=false)
    private int chakraConsumptionPerLevel;

    private int reqLevel;
    
    /**
     * Name of a spell
     */
    @Id
    @Column(length=20, nullable=false)
    private String name;

    /**
     * Getter
     * {@link Spell#baseDamage}
     */
    public int getBaseDamage() {
        return baseDamage;
    }

    public int getReqLevel() {
        return reqLevel;
    }

    public void setReqLevel(int reqLevel) {
        this.reqLevel = reqLevel;
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

    public Spell() {
    }

    public Spell(String name, int baseDamage, int lvldmg, int reqlvl, int baseChCons, int chPerLevel) {
        this.name = name;
        this.chakraConsumptionPerLevel = chPerLevel;
        this.baseChakraConsumption = baseChCons;
        this.reqLevel = reqlvl;
        this.baseDamage = baseDamage;
        this.damagePerLevel = lvldmg;
    }

    public Attack performAttack(int level, float resistance) {
        int damage = (int) ((baseDamage + level * damagePerLevel) * (1 - resistance));
        int chakra = baseChakraConsumption - level * chakraConsumptionPerLevel;
        return new Attack(damage, chakra);
    }

}
