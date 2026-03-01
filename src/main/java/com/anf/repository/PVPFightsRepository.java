package com.anf.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.anf.model.FightPVP;

public interface PVPFightsRepository extends CrudRepository<FightPVP, Integer> {
    @Query("SELECT f from FightPVP f where f.firstFighter = :id OR f.secondFighter = :id")
    List<FightPVP> getUsersPVPFights(@Param("id") int id);
}
