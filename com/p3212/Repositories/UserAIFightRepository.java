package com.p3212.Repositories;

import com.p3212.EntityClasses.UserAIFight;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAIFightRepository extends CrudRepository<UserAIFight, Integer> {
    
}
