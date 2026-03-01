package com.anf.repository;

import com.anf.model.database.AiFightParticipation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAIFightRepository extends CrudRepository<AiFightParticipation, Integer> {}
