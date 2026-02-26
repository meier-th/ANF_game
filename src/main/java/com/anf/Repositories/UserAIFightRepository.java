package com.anf.Repositories;

import com.anf.EntityClasses.UserAIFight;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAIFightRepository extends CrudRepository<UserAIFight, Integer> {
    
}
