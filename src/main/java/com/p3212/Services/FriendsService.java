package com.p3212.Services;

import com.p3212.EntityClasses.Friends;
import com.p3212.EntityClasses.User;
import com.p3212.Repositories.FriendsRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FriendsService {
    @Autowired
    FriendsRepository repository;

    public void addFriend(Friends friends) {
        repository.save(friends);
    }

    public void removeFriend(Friends notFriends) {
        repository.delete(notFriends);
    }
    
    public ArrayList<User> getUsersFriends(User user) {
        String name = user.getLogin();
        List<Friends>friends = repository.getUsersFriends(name);
        ArrayList<User> users = new ArrayList<User>();
        for (Friends fr : friends) {
            if (fr.friends_id.getUser1().getLogin().equals(name))
                users.add(fr.friends_id.getUser2());
            else
                users.add(fr.friends_id.getUser1());
       }
        return users;
    }    
    
}