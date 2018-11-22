package com.p3212.Repositories;

import com.p3212.EntityClasses.Boss;
import org.springframework.data.repository.CrudRepository;

@org.springframework.stereotype.Repository
public interface BossRepository extends CrudRepository<Boss, Integer> {

    @Query("select b from bidju b where b.name = :name")
    public Boss getByName(@Param("name") String name);

}
