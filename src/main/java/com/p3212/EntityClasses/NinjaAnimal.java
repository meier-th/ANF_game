package com.p3212.EntityClasses;

import java.io.Serializable;
import java.util.ArrayList;

public class NinjaAnimal extends Creature implements Serializable {

    public static final ArrayList<NinjaAnimal> animals = new ArrayList<>();
    
    static {
        NinjaAnimal veseliba1 = new NinjaAnimal("Vertet", NinjaAnimalRace.Veseliba, 10, 200, 1);
        NinjaAnimal veseliba2 = new NinjaAnimal("Ubele", NinjaAnimalRace.Veseliba, 35, 500, 10);
        NinjaAnimal bojajumus1 = new NinjaAnimal("Lauva", NinjaAnimalRace.Bojajumus, 30, 50, 1);
        NinjaAnimal bojajumus2 = new NinjaAnimal("Lusis", NinjaAnimalRace.Bojajumus, 120, 150, 10);
        NinjaAnimal lidzsvaru1 = new NinjaAnimal("Erglis", NinjaAnimalRace.Lidzsvaru, 20, 100, 1);
        NinjaAnimal lidzsvaru2 = new NinjaAnimal("Lapsa", NinjaAnimalRace.Lidzsvaru, 70, 250, 10);
        NinjaAnimal rodstvennik1 = new NinjaAnimal("Тётя Срака", NinjaAnimalRace.Bugurt, 18, 250, 1);
        NinjaAnimal rodstvennik2 = new NinjaAnimal("Дядя Бафомет", NinjaAnimalRace.Bugurt, 55, 370, 10);
        animals.add(veseliba1);
        animals.add(veseliba2);
        animals.add(bojajumus1);
        animals.add(bojajumus2);
        animals.add(lidzsvaru1);
        animals.add(lidzsvaru2);
        animals.add(rodstvennik1);
        animals.add(rodstvennik2);
    }
    
    private final String name;

    private final int requiredLevel;

    private final int maxHP;

    private final int damage;

    private final NinjaAnimalRace race;

    public String getName() {
        return name;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public int getDamage() {
        return damage;
    }

    public NinjaAnimalRace getRace() {
        return race;
    }

    private NinjaAnimal(String name, NinjaAnimalRace race, int damage, int hp, int reqlevel) {
        this.damage = damage;
        this.maxHP = hp;
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
        return 0.0f;
    }

    @Override
    public int getLevel() {
        return 1;
    }

    @Override
    public int getMaxHp() {
        return maxHP;
    }

    @Override
    public int getMaxChakra() {
        return 0;
    }
}
