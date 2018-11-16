package com.p3212.Services;

import com.p3212.EntityClasses.Stats;
import com.p3212.Repositories.StatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    @Autowired
    StatsRepository repository;

    public void addStats(Stats stats) {
        repository.save(stats);
    }

    public void removeStats(int id) {
        repository.deleteById(id);
    }   
        
}
