package com.p3212.Services;

import com.p3212.EntityClasses.UserAIFight;
import com.p3212.Repositories.UserAIFightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAIFightService {
    
    @Autowired
    UserAIFightRepository repository;
    
    public void add(UserAIFight uf) {
        repository.save(uf);
    }

}
