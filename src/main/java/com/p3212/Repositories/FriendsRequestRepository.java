package com.p3212.Repositories;


import com.p3212.EntityClasses.FriendRequestCompositeKey;
import com.p3212.EntityClasses.FriendsRequest;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

@org.springframework.stereotype.Repository
public interface FriendsRequestRepository extends Repository<FriendsRequest, FriendRequestCompositeKey> {
    
    @Query("select r from FriendsRequest r where r.request_id.friendUser = :user")
    List<FriendsRequest>getIncomingRequests(@Param("user") String userName);
    
    @Query("select r from FriendsRequest r where r.request_id.requestingUser = :user")
    List<FriendsRequest>getOutgoingRequests(@Param("user") String userName);

    FriendsRequest save(FriendsRequest request);

    void deleteById(FriendRequestCompositeKey id);
}
