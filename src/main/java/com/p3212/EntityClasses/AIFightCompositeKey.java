package com.p3212.EntityClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class AIFightCompositeKey implements Serializable {
    
    /**
     * Date of a fight
     */
    @Column(name="fight_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    
    /**
     * Opponent
     * Either a character or an AI boss
     */
    
    @ManyToOne
    @JoinColumn(name="boss")
    private Boss rival;
    
    @ManyToOne
    @JoinColumn(name="fighter")
    @JsonIgnore
    private Character fighter;

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.date != null ? this.date.hashCode() : 0);
        hash = 67 * hash + (this.rival != null ? this.rival.hashCode() : 0);
        hash = 67 * hash + (this.fighter != null ? this.fighter.hashCode() : 0);
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
        if (this.date != other.date && (this.date == null || !this.date.equals(other.date))) {
            return false;
        }
        if (this.rival != other.rival && (this.rival == null || !this.rival.equals(other.rival))) {
            return false;
        }
        if (this.fighter != other.fighter && (this.fighter == null || !this.fighter.equals(other.fighter))) {
            return false;
        }
        return true;
    }
    
    
    
}
