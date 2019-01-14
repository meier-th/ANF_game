package com.p3212.EntityClasses;

import java.io.Serializable;
import java.util.ArrayList;
import javax.persistence.Column;
import javax.persistence.Id;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Represents NinjaAnimal entity
 * Used to operate on ninja animals
 */
@Entity
@Table(name = "Ninja_animals")
public class NinjaAnimal extends Creature implements Serializable {

    public static final ArrayList<NinjaAnimal> animals = new ArrayList<>();
    
    static {
        NinjaAnimal veseliba1 = new NinjaAnimal("Vertet", new NinjaAnimalRace(NinjaAnimalRace.races.VESELIBA.toString()), 10, 200, 1);
        NinjaAnimal veseliba2 = new NinjaAnimal("Ubele", new NinjaAnimalRace(NinjaAnimalRace.races.VESELIBA.toString()), 35, 500, 10);
        NinjaAnimal bojajumus1 = new NinjaAnimal("Lauva", new NinjaAnimalRace(NinjaAnimalRace.races.BOJAJUMUS.toString()), 30, 50, 1);
        NinjaAnimal bojajumus2 = new NinjaAnimal("Lusis", new NinjaAnimalRace(NinjaAnimalRace.races.BOJAJUMUS.toString()), 120, 150, 10);
        NinjaAnimal lidzsvaru1 = new NinjaAnimal("Erglis", new NinjaAnimalRace(NinjaAnimalRace.races.LIDZSVARU.toString()), 20, 100, 1);
        NinjaAnimal lidzsvaru2 = new NinjaAnimal("Lapsa", new NinjaAnimalRace(NinjaAnimalRace.races.LIDZSVARU.toString()), 70, 250, 10);
        NinjaAnimal rodstvennik1 = new NinjaAnimal("Тётя Срака", new NinjaAnimalRace(NinjaAnimalRace.races.DALNIY_RODSTVENNIK.toString()), 18, 250, 1);
        NinjaAnimal rodstvennik2 = new NinjaAnimal("Дядя Бафомет", new NinjaAnimalRace(NinjaAnimalRace.races.DALNIY_RODSTVENNIK.toString()), 55, 370, 10);
        animals.add(veseliba1);
        animals.add(veseliba2);
        animals.add(bojajumus1);
        animals.add(bojajumus2);
        animals.add(lidzsvaru1);
        animals.add(lidzsvaru2);
        animals.add(rodstvennik1);
        animals.add(rodstvennik2);
    }
    
    /**
     * Name of the ninja animal
     */
    @Id
    @Column(length=30)
    private String name;

    /**
     * The required level to summon the animal
     */
    @Column(nullable=false)
    private int requiredLevel;

    /**
     * HP
     */
    @Column(nullable=false)
    private int hp;

    /**
     * Damage
     */
    @Column(nullable=false)
    private int damage;

    /**
     * Race of the animal
     */
    @ManyToOne
    @JoinColumn(name = "race", nullable=false)
    private NinjaAnimalRace race;

    /**
     * Getter
     * {@link NinjaAnimal#name}
     */
    public String getName() {
        return name;
    }

    /**
     * Setter
     * {@link NinjaAnimal#name}
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter
     * {@link NinjaAnimal#requiredLevel}
     */
    public int getRequiredLevel() {
        return requiredLevel;
    }

    /**
     * Setter
     * {@link NinjaAnimal#requiredLevel}
     */
    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    /**
     * Getter
     * {@link NinjaAnimal#hp}
     */
    public int getHp() {
        return hp;
    }

    /**
     * Setter
     * {@link NinjaAnimal#hp}
     */
    public void setHp(int hp) {
        this.hp = hp;
    }

    /**
     * Getter
     * {@link NinjaAnimal#damage}
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Setter
     * {@link NinjaAnimal#damage}
     */
    public void setDamage(int damage) {
        this.damage = damage;
    }

    /**
     * Getter
     * {@link NinjaAnimal#race}
     */
    public NinjaAnimalRace getRace() {
        return race;
    }

    /**
     * Setter
     * {@link NinjaAnimal#race}
     */
    public void setRace(NinjaAnimalRace race) {
        this.race = race;
    }

    public NinjaAnimal() {
    }

    public NinjaAnimal(String name, NinjaAnimalRace race, int damage, int hp, int reqlevel) {
        this.damage = damage;
        this.hp = hp;
        this.name = name;
        this.race = race;
        this.requiredLevel = reqlevel;
    }

    @Override
    public void acceptDamage(int damage) {
        currentHP -= damage;
    }

    @Override
    public float getResistance() {
        return 0.25f;
    }

    @Override
    public int getLevel() {
        return requiredLevel / 2;
    }

    @Override
    public int getMaxHp() {
        return hp;
    }

    @Override
    public int getMaxChakra() {
        return requiredLevel * 100;
    }
}
