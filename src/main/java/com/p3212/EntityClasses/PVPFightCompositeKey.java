package com.p3212.EntityClasses;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class PVPFightCompositeKey implements Serializable {
    @ManyToOne
    @JoinColumn(name = "firstFighter")
    private Character firstFighter;

    @ManyToOne
    @JoinColumn(name = "secondFighter")
    private Character secondFighter;

    @Column(name = "fight_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fightDate;

    @Override
    public int hashCode() {
        int hash = 3;
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
        final PVPFightCompositeKey other = (PVPFightCompositeKey) obj;
        if (this.firstFighter != other.firstFighter && (this.firstFighter == null || !this.firstFighter.equals(other.firstFighter))) {
            return false;
        }
        if (this.secondFighter != other.secondFighter && (this.secondFighter == null || !this.secondFighter.equals(other.secondFighter))) {
            return false;
        }
        if (this.fightDate != other.fightDate && (this.fightDate == null || !this.fightDate.equals(other.fightDate))) {
            return false;
        }
        return true;
    }

    public Character getFirstFighter() {
        return firstFighter;
    }

    public void setFirstFighter(Character firstFighter) {
        this.firstFighter = firstFighter;
    }

    public Character getSecondFighter() {
        return secondFighter;
    }

    public void setSecondFighter(Character secondFighter) {
        this.secondFighter = secondFighter;
    }

    public void setFightDate(Date fightDate) {
        this.fightDate = fightDate;
    }

}
