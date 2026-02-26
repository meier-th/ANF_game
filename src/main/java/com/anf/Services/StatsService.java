package com.anf.Services;

import com.anf.EntityClasses.Stats;
import com.anf.Repositories.StatsRepository;
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
