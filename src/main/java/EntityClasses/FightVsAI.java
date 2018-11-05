package EntityClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
     * Used to operate on fight history data
     */
@Entity
@Table(name="ai_fights")
public class FightVsAI {
    
    public static enum Result {
        WON,
        LOST,
        DIED;
    }
    
    /**
     * Date of a fight
     */
    
    @Id
    @GeneratedValue
    private int id;
    
    private Date date;
    
    /**
     * The rating change for user
     */
    private int ratingChange;
    
    /**
     * Opponent
     * Either a character or an AI boss
     */
    
    @ManyToOne
    @JoinColumn(name="boss")
    private Boss rival;
    
    /**
     * Result - won, lost, died
     */
    private Result result;
    
    
    @ManyToOne
    @JoinColumn(name="fighter")
    @JsonIgnore
    private Character fighter;
    
    
    /**
     * Getter
     * {@link FightVsAI#date} 
     */
    public Date getDate() {
        return date;
    }

    /**
     * Setter
     * {@link FightVsAI#date} 
     */
    public void setDate(Date date) {
        this.date = date;
    }

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
     * {@link FightVsAI#rival} 
     */
    public Boss getRival() {
        return rival;
    }

    /**
     * Setter
     * {@link FightVsAI#rival} 
     */
    public void setRival(Boss rival) {
        this.rival = rival;
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
     * @param date
     * @param rival
     * @param result
     * @param ratingChange 
     */
    public FightVsAI(Date date, Boss rival, Result result, int ratingChange) {
        this.date = date;
        this.rival = rival;
        this.result = result;
        this.ratingChange = ratingChange;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Character getFighter() {
        return fighter;
    }

    public void setFighter(Character fighter) {
        this.fighter = fighter;
    }
    
}
