package com.p3212.EntityClasses;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


public class AIFightCompositeKey implements Serializable {
    
    /**
     * Date of a fight
     */
    @Column(name="fight_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fight_date;
    
    /**
     * Opponent
     * Either a character or an AI boss
     */
    
    @ManyToOne
    @JoinColumn(name="boss")
    private Boss boss;


    public Boss getBoss() {
        return boss;
    }

    public void setBoss(Boss boss) {
        this.boss = boss;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.fight_date != null ? this.fight_date.hashCode() : 0);
        hash = 67 * hash + (this.boss != null ? this.boss.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AIFightCompositeKey other = (AIFightCompositeKey) obj;
        if (this.fight_date != other.fight_date && (this.fight_date == null || !this.fight_date.equals(other.fight_date))) {
            return false;
        }
        if (this.boss != other.boss && (this.boss == null || !this.boss.equals(other.boss))) {
            return false;
        }
        return true;
    }
    
    
    
}
