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
        animals1 = new ArrayList<>();
        animals2 = new ArrayList<>();

    }

    protected final int id;

    private static volatile int number;

    private int currentAttacker = -1;

    protected ArrayList<User> fighters;

    protected User fighter1;

    protected User fighter2;

    protected ArrayList<NinjaAnimal> animals1;

    protected ArrayList<NinjaAnimal> animals2;

    protected String currentName = "";

    protected long timeLeft;

    public long getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(long timeLeft) {
        this.timeLeft = timeLeft;
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

    public void addFighter(Character character) {
        fighters.add(character.getUser());
    }

    public void switchAttacker() {
        if (this instanceof FightPVP) {
            currentAttacker = (currentAttacker + 1) % (2 + animals1.size() + animals2.size());
            // 0 - first, 1 - second, 2 - animals1[0], 3 - animals2[0]
        } else {
            currentAttacker = (currentAttacker + 1) % (fighters.size() + animals1.size() + 1);
            // 0-4 fighters[i], 5 - boss, 6-10 - animals1[i]
        }
        currentName = getCurrentAttacker(0);
    }

    public String getCurrentAttacker(int offset) {
        if (this instanceof FightPVP) {
            switch ((currentAttacker + offset) % (2 + animals1.size() + animals2.size())) {
                case 0:
                    return fighter1.getLogin();

                case 1:
                    return fighter2.getLogin();

                case 2:
                    return animals1.get(0).getName().substring(0, 3)+'1';

                case 3:
                    return animals2.get(0).getName().substring(0, 3)+'0';
            }
        } else {
            if ((currentAttacker + offset) % (1 + animals1.size() + fighters.size()) < fighters.size()) {
                return fighters.get((currentAttacker + offset) % (1 + animals1.size() + fighters.size())).getLogin();
            }
            if ((currentAttacker + offset) % (1 + fighters.size() + animals2.size()) == fighters.size()) {
                return String.valueOf(((FightVsAI) this).getBoss().getNumberOfTails());
            } else {
                return animals1.get(
                        ((currentAttacker + offset) % (1 + animals1.size() + fighters.size())) -
                                fighters.size() - 1).getName().substring(0, 3);
            }
        }
        return null;
    }

    public String getNextAttacker() {
        return getCurrentAttacker(1);
    }

    public ArrayList<User> getFighters() {
        return fighters;
    }

    public ArrayList<NinjaAnimal> getAnimals1() {
        return animals1;
    }

    public ArrayList<NinjaAnimal> getAnimals2() {
        return animals2;
    }

    
    
}
