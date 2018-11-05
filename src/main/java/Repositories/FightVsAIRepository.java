package Repositories;

import EntityClasses.FightVsAI;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface FightVsAIRepository extends CrudRepository<FightVsAI, Integer> {
    @Query("SELECT f FROM FightVsAI f WHERE f.fighter = :id")
    List<FightVsAI> getAIFightsByUser(@Param("id") int id);
}
