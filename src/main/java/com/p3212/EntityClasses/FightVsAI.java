package com.p3212.EntityClasses;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EmbeddedId;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
     * Used to operate on fight history data
     */
@Entity
@Table(name="ai_fights")
public class FightVsAI extends Fight {
    
    @EmbeddedId
    AIFightCompositeKey aiId;
    
    @OneToMany(mappedBy="fight")
    private List<UserAIFight> setFighters;

    public AIFightCompositeKey getAiId() {
        return aiId;
    }

    public void setAiId(AIFightCompositeKey aiId) {
        this.aiId = aiId;
    }

    public List<UserAIFight> getSetFighters() {
        return setFighters;
    }

    public void setSetFighters(List<UserAIFight> fighters) {
        this.setFighters = fighters;
    }
    
    
    
}
