package com.p3212.Repositories;


import com.p3212.EntityClasses.FriendsRequest;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendsRequestRepository extends CrudRepository<FriendsRequest, Integer> {
    
    @Query("select r from FriendsRequest r where r.request_id.friendUser = :user")
    List<FriendsRequest>getIncomingRequests(@Param("user") String userName);
    
    @Query("select r from FriendsRequest r where r.request_id.requestingUser = :user")
    List<FriendsRequest>getOutgoingRequests(@Param("user") String userName);

}
