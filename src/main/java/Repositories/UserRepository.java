package Repositories;

import EntityClasses.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 *
 * @author Maestro
 */

@Repository
public interface UserRepository extends CrudRepository<User, String>{
    
}
