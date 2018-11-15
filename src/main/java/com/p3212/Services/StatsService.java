package com.p3212.Services;

import com.p3212.EntityClasses.Stats;
import com.p3212.EntityClasses.User;
import com.p3212.Repositories.StatsRepository;
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
    
    
    
}
