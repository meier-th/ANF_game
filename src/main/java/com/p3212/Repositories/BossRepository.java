package com.p3212.Repositories;

import com.p3212.EntityClasses.Boss;
import org.springframework.data.repository.CrudRepository;

@org.springframework.stereotype.Repository
public interface BossRepository extends CrudRepository<Boss, String> {
}
