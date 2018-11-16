package com.p3212.EntityClasses;

public abstract class Creature {
    public abstract void acceptDamage(int damage);

    public abstract float getResistance();

    public abstract int getLevel();

    public abstract int getMaxHp();

    public abstract int getMaxChakra();

    protected int currentHP;
    protected int currentChakra;

    public int getCurrentHP() {
        return currentHP;
    }

    public void setCurrentHP(int currentHP) {
        this.currentHP = currentHP;
    }

    public int getCurrentChakra() {
        return currentChakra;
    }

    public void setCurrentChakra(int currentChakra) {
        this.currentChakra = currentChakra;
    }

    public void prepareForFight() {
        currentHP = getMaxHp();
        currentChakra = getMaxChakra();
    }
}
