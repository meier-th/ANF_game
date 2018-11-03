package P3212.ANFBackend.Repositories;

import P3212.ANFBackend.EntityClasses.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

}
