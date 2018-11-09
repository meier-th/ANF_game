package Repositories;

import EntityClasses.Boss;
import org.springframework.data.repository.CrudRepository;

@org.springframework.stereotype.Repository
public interface BossRepository extends CrudRepository<Boss, String> {
}
