package Repositories;

import EntityClasses.Friends;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendsRepository extends CrudRepository<Friends, Integer> {
}
