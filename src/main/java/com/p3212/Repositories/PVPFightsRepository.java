package com.p3212.Repositories;

import com.p3212.EntityClasses.FightPVP;
import com.p3212.EntityClasses.PVPFightCompositeKey;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PVPFightsRepository extends CrudRepository<FightPVP, PVPFightCompositeKey> {
    @Query("SELECT f from FightPVP f where f.firstFighter = :id OR f.secondFighter = :id")
    List<FightPVP> getUsersPVPFights(@Param("id") int id);
}
