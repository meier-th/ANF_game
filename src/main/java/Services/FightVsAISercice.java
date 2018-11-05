package Services;

import EntityClasses.FightVsAI;
import Repositories.FightVsAIRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FightVsAISercice {
    @Autowired
    private FightVsAIRepository repository;
    
    void addFight(FightVsAI fight) {
        repository.save(fight);
    }
    
    List<FightVsAI> getByUserId(int id) {
        return repository.getAIFightsByUser(id);
    }
    
    FightVsAI getFight(int id) {
        return repository.findById(id).get();
    }
}
