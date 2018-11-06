package Repositories;

import EntityClasses.AIFightCompositeKey;
import EntityClasses.FightVsAI;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface FightVsAIRepository extends CrudRepository<FightVsAI, AIFightCompositeKey> {
    @Query("SELECT f FROM FightVsAI f WHERE f.aiId.fighter = :id")
    List<FightVsAI> getAIFightsByUser(@Param("id") int id);
}
