package com.p3212.EntityClasses;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="pvp_fights")
public class FightPVP {
    
    
    @EmbeddedId
    PVPFightCompositeKey pvpId;
    
    private boolean firstWon;
    
    private int ratingChange;

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
