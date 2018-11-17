package com.p3212.Services;

import com.p3212.EntityClasses.AIFightCompositeKey;
import com.p3212.EntityClasses.FightVsAI;
import com.p3212.Repositories.FightVsAIRepository;
import java.util.List;
import com.p3212.EntityClasses.Character;
import com.p3212.EntityClasses.UserAIFight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FightVsAIService {
    @Autowired
    private FightVsAIRepository repository;
    
    public void addFight(FightVsAI fight) {
        repository.save(fight);
    }
    
    public List<UserAIFight> getByFighterId(Character ch) {
        int id = ch.getId();
        return repository.getAIFightsByUser(id);
    }
    
    public FightVsAI getFight(AIFightCompositeKey id) {
        return repository.findById(id).get();
    }
}
