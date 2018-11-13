package com.p3212.EntityClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class SpellHandlingCompositeKey implements Serializable {
    /**
     * Character
     */
    @ManyToOne
    @JoinColumn(name="character")
    @JsonIgnore
    private Character characterHandler;
    
    /**
     * Spell
     */
    
    @ManyToOne
    @JoinColumn(name="spell")
    private Spell spellUse;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.characterHandler != null ? this.characterHandler.hashCode() : 0);
        hash = 97 * hash + (this.spellUse != null ? this.spellUse.hashCode() : 0);
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
        final SpellHandlingCompositeKey other = (SpellHandlingCompositeKey) obj;
        if (this.characterHandler != other.characterHandler && (this.characterHandler == null || !this.characterHandler.equals(other.characterHandler))) {
            return false;
        }
        if (this.spellUse != other.spellUse && (this.spellUse == null || !this.spellUse.equals(other.spellUse))) {
            return false;
        }
        return true;
    }
    
    
    
}
