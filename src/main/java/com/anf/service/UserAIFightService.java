package com.anf.service;

import org.springframework.stereotype.Service;

import com.anf.model.UserAIFight;
import com.anf.repository.UserAIFightRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor 
public class UserAIFightService {
    
    private final UserAIFightRepository repository;
    
    public void add(UserAIFight uf) {
        repository.save(uf);
    }

}
