package com.anf.infrastructure.persistence.repository;

import com.anf.model.database.AiFightParticipation;
import com.anf.model.database.FightVsAI;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FightVsAIRepository extends CrudRepository<FightVsAI, Integer> {
  @Query("SELECT uf FROM AiFightParticipation uf WHERE uf.fighter.id = :id ORDER BY uf.fight.fight_date DESC")
  List<AiFightParticipation> getAIFightsByUser(@Param("id") int id);
}
