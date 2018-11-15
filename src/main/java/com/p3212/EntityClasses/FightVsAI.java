package com.p3212.EntityClasses;

import javax.persistence.Entity;
import javax.persistence.EmbeddedId;
import javax.persistence.Table;

/**
     * Used to operate on fight history data
     */
@Entity
@Table(name="ai_fights")
public class FightVsAI extends Fight {
    
    @EmbeddedId
    AIFightCompositeKey aiId;
    
    public static enum Result {
        WON,
        LOST,
        DIED;
    }
    
    /**
     * The rating change for user
     */
    private int ratingChange;
    
    
    
    /**
     * Result - won, lost, died
     */
    private Result result;
    
    /**
     * Getter
     * {@link FightVsAI#ratingChange} 
     */
    public int getRatingChange() {
        return ratingChange;
    }

    /**
     * Setter
     * {@link FightVsAI#ratingChange} 
     */
    public void setRatingChange(int ratingChange) {
        this.ratingChange = ratingChange;
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
    
    /**
     * Default constructor
     * Used for dependency injection
     */
    public FightVsAI(){}
    
    /**
     * To be used when retrieved from database
     * @param result
     * @param ratingChange 
     */
    public FightVsAI(Result result, int ratingChange) {
        this.result = result;
        this.ratingChange = ratingChange;
    }
    
}
