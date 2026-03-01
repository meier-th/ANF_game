package com.anf.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.anf.model.UserAIFight;

@Repository
public interface UserAIFightRepository extends CrudRepository<UserAIFight, Integer> {
    
}
