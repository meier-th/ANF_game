package com.anf.Services;

import com.anf.EntityClasses.FightVsAI;
import com.anf.Repositories.FightVsAIRepository;
import java.util.List;
import com.anf.EntityClasses.Character;
import com.anf.EntityClasses.UserAIFight;
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
    
    public FightVsAI getFight(int id) {
        return repository.findById(id).get();
    }
}
