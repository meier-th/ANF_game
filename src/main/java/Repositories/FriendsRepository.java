package Repositories;

import EntityClasses.Friends;
import EntityClasses.FriendsCompositeKey;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendsRepository extends CrudRepository<Friends, FriendsCompositeKey> {

    @Query("select f from Friends f where f.friends_id.user1 = :user or f.friends_id.user2 = :user")
    List<Friends>getUsersFriends(@Param("user") String username);

}
