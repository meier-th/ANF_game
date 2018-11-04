package Repositories;

import EntityClasses.Boss;
import org.springframework.data.repository.Repository;

@org.springframework.stereotype.Repository
public interface BossRepository extends Repository<Boss, Integer> {

    Boss save(Boss newOne);

    Boss findById(int id);
}
