package P3212.ANFBackend.Repositories;

import P3212.ANFBackend.EntityClasses.Appearance;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppearanceRepository extends CrudRepository<Appearance, Long> {

}
