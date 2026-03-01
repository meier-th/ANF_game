package com.anf.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anf.model.FightVsAI;
import com.anf.model.UserAIFight;

@Repository
public interface FightVsAIRepository extends CrudRepository<FightVsAI, Integer> {
    @Query("SELECT uf FROM FightVsAI f inner join UserAIFight uf WHERE uf.id = :id")
    List<UserAIFight> getAIFightsByUser(@Param("id") int id);
}
