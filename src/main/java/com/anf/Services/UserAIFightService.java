package com.anf.Services;

import com.anf.EntityClasses.UserAIFight;
import com.anf.Repositories.UserAIFightRepository;
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
