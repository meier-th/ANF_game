package Services;

import EntityClasses.Friends;
import Repositories.FriendsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FriendsService {

    @Autowired
    FriendsRepository repository;

    void addFriend(Friends friends) {
        repository.save(friends);
    }

    void removeFriend(Friends notFriends) {
        repository.delete(notFriends);
    }
}
