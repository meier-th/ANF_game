package com.anf.Repositories;

import com.anf.EntityClasses.Boss;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BossRepository extends CrudRepository<Boss, Integer> {

    @Query("select b from Boss b where b.name = :name")
    public Boss getByName(@Param("name") String name);

}
