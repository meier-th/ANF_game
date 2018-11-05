package EntityClasses;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="pvp_fights")
public class FightPVP {
    @Id
    @GeneratedValue
    int id;
    
    @ManyToOne
    @JoinColumn(name="firstFighter")
    private Character firstFighter;
    
    @ManyToOne
    @JoinColumn(name="secondFighter")
    private Character secondFighter;
    
    private Date fightDate;
    
    private boolean firstWon;
    
    private int ratingChange;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Character getFirstFighter() {
        return firstFighter;
    }

    public void setFirstFighter(Character firstFighter) {
        this.firstFighter = firstFighter;
    }

    public Character getSecondCharacter() {
        return secondFighter;
    }

    public void setSecondCharacter(Character secondCharacter) {
        this.secondFighter = secondCharacter;
    }

    public Date getFightDate() {
        return fightDate;
    }

    public void setFightDate(Date fightDate) {
        this.fightDate = fightDate;
    }

    public boolean isFirstWon() {
        return firstWon;
    }

    public void setFirstWon(boolean firstWon) {
        this.firstWon = firstWon;
    }

    public int getRatingChange() {
        return ratingChange;
    }

    public void setRatingChange(int ratingChange) {
        this.ratingChange = ratingChange;
    }
    
    
}
