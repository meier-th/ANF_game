package Services;

import EntityClasses.SpellHandling;
import EntityClasses.Character;
import Repositories.SpellHandlingRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpellHandlingService {

    @Autowired
    SpellHandlingRepository repository;
    
    public void addHandling(SpellHandling sh) {
        repository.save(sh);
    }
    
    public void updateHandling(SpellHandling sh) {
        repository.save(sh);
    }
    
    public List<SpellHandling> getPersonsHandling (Character ch) {
        int id = ch.getId();
        return repository.getCharactersHandlings(id); 
    }
    
}
