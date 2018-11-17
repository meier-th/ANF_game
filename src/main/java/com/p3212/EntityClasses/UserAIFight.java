package com.p3212.EntityClasses;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="user_aifight")
public class UserAIFight {
    
    public static enum Result {
        WON,
        LOST,
        DIED;
    }
    
    @Id
    @GeneratedValue
    private int fIdentity;
    
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="boss", referencedColumnName="boss"),
        @JoinColumn(name="fight_date", referencedColumnName="fight_date")
    })
    private FightVsAI fight;

    @ManyToOne
    @JoinColumn(name="id", referencedColumnName="id")
    private Character fighter;

    /**
     * Result - won, lost, died
     */
    @Column(name="fresult")
    @Enumerated(EnumType.STRING)
    private Result result;
    
    public int getId() {
        return fIdentity;
    }

    public FightVsAI getFight() {
        return fight;
    }

    public Character getFighter() {
        return fighter;
    }
    
    /**
     * Getter
     * {@link FightVsAI#result} 
     */
    public Result getResult() {
        return result;
    }

    /**
     * Setter
     * {@link FightVsAI#result} 
     */
    public void setResult(Result result) {
        this.result = result;
    }
    
}
