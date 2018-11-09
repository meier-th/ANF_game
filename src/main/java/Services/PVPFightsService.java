package Services;

import EntityClasses.FightPVP;
import EntityClasses.PVPFightCompositeKey;
import Repositories.PVPFightsRepository;
import java.util.List;
import EntityClasses.Character;
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
