package com.anf.repository;

import com.anf.model.UserAIFight;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAIFightRepository extends CrudRepository<UserAIFight, Integer> {}
