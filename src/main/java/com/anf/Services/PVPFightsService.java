package com.anf.Services;

import com.anf.EntityClasses.FightPVP;
import com.anf.Repositories.PVPFightsRepository;
import java.util.List;
import com.anf.EntityClasses.Character;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PVPFightsService {
    @Autowired
    private PVPFightsRepository repository;
    
    public void addFight(FightPVP fight) {
        repository.save(fight);
    }
    
    public List<FightPVP> getUsersFights(Character ch) {
        int id = ch.getId();
        return repository.getUsersPVPFights(id);
    }
    
    public FightPVP getFight(int id) {
        return repository.findById(id).get();
    }
}
