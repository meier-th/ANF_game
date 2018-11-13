package com.p3212.Services;

import com.p3212.EntityClasses.AIFightCompositeKey;
import com.p3212.EntityClasses.FightVsAI;
import com.p3212.Repositories.FightVsAIRepository;
import java.util.List;
import com.p3212.EntityClasses.Character;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FightVsAISercice {
    @Autowired
    private FightVsAIRepository repository;
    
    void addFight(FightVsAI fight) {
        repository.save(fight);
    }
    
    List<FightVsAI> getByFighterId(Character ch) {
        int id = ch.getId();
        return repository.getAIFightsByUser(id);
    }
    
    FightVsAI getFight(AIFightCompositeKey id) {
        return repository.findById(id).get();
    }
}
