package Services;

import EntityClasses.AIFightCompositeKey;
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
    
    /*List<FightVsAI> getByUserId(AIFightCompositeKey id) {
        return repository.getAIFightsByUser(id);
    }*/
    
    FightVsAI getFight(AIFightCompositeKey id) {
        return repository.findById(id).get();
    }
}