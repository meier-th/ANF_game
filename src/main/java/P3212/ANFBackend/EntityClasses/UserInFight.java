package P3212.ANFBackend.EntityClasses;

import java.time.LocalDate;

/**
     * Used to operate on fight history data
     */
public class UserInFight {
    
    public static enum Result {
        WON,
        LOST,
        DIED;
    }
    
    /**
     * Date of a fight
     */
    private LocalDate date;
    
    /**
     * The rating change for user
     */
    private int ratingChange;
    
    /**
     * Opponent
     * Either a character or an AI boss
     */
    private Creature rival;
    
    /**
     * Result - won, lost, died
     */
    private Result result;

    /**
     * Getter
     * {@link UserInFight#date} 
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Setter
     * {@link UserInFight#date} 
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Getter
     * {@link UserInFight#ratingChange} 
     */
    public int getRatingChange() {
        return ratingChange;
    }

    /**
     * Setter
     * {@link UserInFight#ratingChange} 
     */
    public void setRatingChange(int ratingChange) {
        this.ratingChange = ratingChange;
    }

    /**
     * Getter
     * {@link UserInFight#rival} 
     */
    public Creature getRival() {
        return rival;
    }

    /**
     * Setter
     * {@link UserInFight#rival} 
     */
    public void setRival(Creature rival) {
        this.rival = rival;
    }

    /**
     * Getter
     * {@link UserInFight#result} 
     */
    public Result getResult() {
        return result;
    }

    /**
     * Setter
     * {@link UserInFight#result} 
     */
    public void setResult(Result result) {
        this.result = result;
    }
    
    /**
     * Default constructor
     * Used for dependency injection
     */
    public UserInFight(){}
    
    /**
     * To be used when retrieved from database
     * @param date
     * @param rival
     * @param result
     * @param ratingChange 
     */
    public UserInFight(LocalDate date, Creature rival, Result result, int ratingChange) {
        this.date = date;
        this.rival = rival;
        this.result = result;
        this.ratingChange = ratingChange;
    }
    
}
