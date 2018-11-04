package Repositories;

import EntityClasses.PrivateMessage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for message entity
 */
@Repository
public interface MessagesRepository extends CrudRepository<PrivateMessage, Integer> {

    @Query("update PrivateMessages p set isRead = true where p.id = :id")
    void setRead(@Param("id") int id);
}
