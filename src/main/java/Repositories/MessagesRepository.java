package Repositories;

import EntityClasses.PrivateMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for message entity
 */
@Repository
public interface MessagesRepository extends CrudRepository<PrivateMessage, Integer> {
}
