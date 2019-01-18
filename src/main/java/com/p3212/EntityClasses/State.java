package com.p3212.EntityClasses;

public class State {

private String attacker;
private String target;
private String attackName;
private int chakraCost;
private int damage;
private int chakraBurn;
private boolean deadly;
private boolean everyoneDead;
private String nextAttacker;

    public String getAttacker() {
        return attacker;
    }

    public void setAttacker(String attacker) {
        this.attacker = attacker;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getAttackName() {
        return attackName;
    }

    public void setAttackName(String attackName) {
        this.attackName = attackName;
    }

    public int getChakraCost() {
        return chakraCost;
    }

    public void setChakraCost(int chakraCost) {
        this.chakraCost = chakraCost;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getChakraBurn() {
        return chakraBurn;
    }

    public void setChakraBurn(int chakraBurn) {
        this.chakraBurn = chakraBurn;
    }

    public boolean isDeadly() {
        return deadly;
    }

    public void setDeadly(boolean deadly) {
        this.deadly = deadly;
    }

    public boolean isEveryoneDead() {
        return everyoneDead;
    }

    public void setEveryoneDead(boolean everyoneDead) {
        this.everyoneDead = everyoneDead;
    }

    public String getNextAttacker() {
        return nextAttacker;
    }

    public void setNextAttacker(String nextAttacker) {
        this.nextAttacker = nextAttacker;
    }

    public State(String attacker, String target, String attackName, int chakraCost, int damage, int chakraBurn, boolean deadly, boolean everyoneDead, String nextAttacker) {
        this.attacker = attacker;
        this.target = target;
        this.attackName = attackName;
        this.chakraCost = chakraCost;
        this.damage = damage;
        this.chakraBurn = chakraBurn;
        this.deadly = deadly;
        this.everyoneDead = everyoneDead;
        this.nextAttacker = nextAttacker;
    }
    
    public State(){}
    
}
