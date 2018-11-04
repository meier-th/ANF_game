package Repositories;

import EntityClasses.Friends;
import EntityClasses.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendsRepository extends org.springframework.data.repository.Repository<Friends, String> {

    Friends save(Friends friends);

    void delete(Friends notFriends);

    Iterable<Friends> findAllByUser1(User user); //TODO Here should be select with join
}
