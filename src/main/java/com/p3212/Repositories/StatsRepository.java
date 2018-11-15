package com.p3212.Repositories;

import com.p3212.EntityClasses.Stats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatsRepository extends CrudRepository<Stats, Integer> {
    @Query("Select s from Stats s order by s.rating DESC")
    Page<Stats>getTopStats(Pageable pg);
    
    @Query("update Stats s set s.level = s.level + 1, s.upgradePoints = s.upgradePoints + 3 where s.id = :i")
    void levelUp(@Param("i") int id);
    
}
