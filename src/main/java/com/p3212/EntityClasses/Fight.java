package com.p3212.EntityClasses;

/**
 * Represents statistics during a fight
 */

public class Fight {
    
    public Fight() {
        synchronized(Fight.class) {
            number++;
        }
        this.id = number;
    }
    
    private final int id;
    
    private static volatile int number;
    
    private int fighter1HP;

    private int fighter2HP;

    private int fighter1Chakra;

    private int fighter2Chakra;



    public void setFighter1HP(int fighter1HP) {
        this.fighter1HP = fighter1HP;
    }

    public void setFighter2HP(int fighter2HP) {
        this.fighter2HP = fighter2HP;
    }

    public void setFighter1Chakra(int fighter1Chakra) {
        this.fighter1Chakra = fighter1Chakra;
    }

    public void setFighter2Chakra(int fighter2Chakra) {
        this.fighter2Chakra = fighter2Chakra;
    }

    public int getFighter1HP() {
        return fighter1HP;
    }

    public int getFighter2HP() {
        return fighter2HP;
    }

    public int getFighter1Chakra() {
        return fighter1Chakra;
    }


    public int getFighter2Chakra() {
        return fighter2Chakra;
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
