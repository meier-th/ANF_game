package com.anf.repository;

import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.FightVsAI;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FightVsAIRepository extends CrudRepository<FightVsAI, Integer> {
  @Query("SELECT uf FROM FightVsAI f inner join AiFightParticipation uf WHERE uf.id = :id")
  List<AiFightParticipation> getAIFightsByUser(@Param("id") int id);
}
