package com.anf.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.anf.model.GameCharacter;
import com.anf.model.FightVsAI;
import com.anf.model.UserAIFight;
import com.anf.repository.FightVsAIRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FightVsAIService {
    private final FightVsAIRepository repository;
    
    public void addFight(FightVsAI fight) {
        repository.save(fight);
    }
    
    public List<UserAIFight> getByFighterId(GameCharacter ch) {
        int id = ch.getId();
        return repository.getAIFightsByUser(id);
    }
    
    public FightVsAI getFight(int id) {
        return repository.findById(id).get();
    }
}
