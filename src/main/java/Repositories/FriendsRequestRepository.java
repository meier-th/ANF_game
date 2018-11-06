package Repositories;


import EntityClasses.FriendRequestCompositeKey;
import EntityClasses.FriendsRequest;
import org.springframework.data.repository.Repository;

@org.springframework.stereotype.Repository
public interface FriendsRequestRepository extends Repository<FriendsRequest, FriendRequestCompositeKey> {

    FriendsRequest save(FriendsRequest request);

    void deleteById(FriendRequestCompositeKey id);
}
