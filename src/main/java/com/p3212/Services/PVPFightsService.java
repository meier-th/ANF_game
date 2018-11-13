package com.p3212.Services;

import com.p3212.EntityClasses.FightPVP;
import com.p3212.EntityClasses.PVPFightCompositeKey;
import com.p3212.Repositories.PVPFightsRepository;
import java.util.List;
import com.p3212.EntityClasses.Character;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PVPFightsService {
    @Autowired
    private PVPFightsRepository repository;
    
    void addFight(FightPVP fight) {
        repository.save(fight);
    }
    
    List<FightPVP> getUsersFights(Character ch) {
        int id = ch.getId();
        return repository.getUsersPVPFights(id);
    }
    
    FightPVP getFight(PVPFightCompositeKey id) {
        return repository.findById(id).get();
    }
}
