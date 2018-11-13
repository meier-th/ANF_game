package EntityClasses;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;

@Entity
@Table(name = "pvp_fights")
public class FightPVP extends Fight {

    @EmbeddedId
    private PVPFightCompositeKey pvpId;

    private boolean firstWon;

    private int ratingChange;

    public PVPFightCompositeKey getPvpId() {
        return pvpId;
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

    public void setFighters(Character char1, Character char2) {
        pvpId.setFirstFighter(char1);
        pvpId.setSecondFighter(char2);

    }
}
