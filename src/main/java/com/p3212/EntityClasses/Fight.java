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

    private final int id;

    private static volatile int number;

    private int currentAttacker = 0;

    private ArrayList<Pair<Integer, Creature>> fighters; //Pair of side and fighter

    public void addFighter(Creature fighter, int side) {
        fighters.add(new Pair<>(side, fighter));
    }

    public ArrayList<Pair<Integer, Creature>> getFighters() {
        return fighters;
    }

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

    public void switchAttacker() {
        currentAttacker = (currentAttacker + 1) % fighters.size();
    }

    @Override
    public String toString() {
        return "{\n\"fightId\": " + id + "\n}";
    }
}
