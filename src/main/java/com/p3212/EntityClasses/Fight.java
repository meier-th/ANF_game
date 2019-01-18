package com.p3212.EntityClasses;

import java.util.ArrayList;

/**
 * Represents statistics during a fight
 */

public class Fight {

    public Fight() {
        synchronized (Fight.class) {
            number++;
        }
        fighters = new ArrayList<>();
        this.id = number;
    }

    protected final int id;

    private static volatile int number;

    private int currentAttacker = 0;

    protected ArrayList<User> fighters;

    protected User fighter1;

    protected User fighter2;

    protected ArrayList<NinjaAnimal> animals1;

    protected ArrayList<NinjaAnimal> animals2;

    public int getId() {
        return id;
    }

    public static int getNumber() {
        return number;
    }

    public static void setNumber(int number) {
        Fight.number = number;
    }

    public int getCurrentAttacker() {
        return currentAttacker;
    }

    public void addFighter(Character character) {
        fighters.add(character.getUser());
    }

    public void switchAttacker() {
//        currentAttacker = (currentAttacker + 1) % (fighters.size() + (fighter1 != null ? 2 : 0) + animals1.size() + animals2.size());
    }

}
