package com.p3212.EntityClasses;

import javafx.util.Pair;

import javax.persistence.GeneratedValue;
import javax.persistence.Transient;
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


}
