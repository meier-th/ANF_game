package com.p3212.Repositories;

import com.p3212.EntityClasses.Boss;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BossRepository extends CrudRepository<Boss, Integer> {

    @Query("select b from Boss b where b.name = :name")
    public Boss getByName(@Param("name") String name);

}
