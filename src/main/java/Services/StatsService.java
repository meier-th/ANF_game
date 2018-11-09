package Services;

import EntityClasses.Stats;
import EntityClasses.User;
import Repositories.StatsRepository;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    @Autowired
    StatsRepository repository;

    void addStats(Stats stats) {
        repository.save(stats);
    }

    void updateStats(Stats stats) {
        repository.save(stats);
    }

    void removeStats(int id) {
        repository.deleteById(id);
    }
    
    void levelUp(User us) {
        Stats stts = us.getStats();
        int id = stts.getId();
        repository.levelUp(id);
    }
    
    ArrayList<User> getTopUsers(int number) {
        ArrayList<User> users = new ArrayList<User>();
        Page<Stats> stts = repository.getTopStats(new PageRequest(0, number));
        for (Stats st : stts){
            users.add(st.user);
        }
        return users;
    }
    
}
