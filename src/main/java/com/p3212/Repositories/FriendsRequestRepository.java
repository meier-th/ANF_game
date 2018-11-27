package com.p3212.Repositories;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.p3212.EntityClasses.FriendsRequest;
import com.p3212.EntityClasses.User;

@Repository
public interface FriendsRequestRepository extends CrudRepository<FriendsRequest, Integer> {
    
    @Query("select r from FriendsRequest r where r.friendUser = :user")
    List<FriendsRequest>getIncomingRequests(@Param("user") String userName);
    
    @Query("select r from FriendsRequest r where r.requestingUser = :user")
    List<FriendsRequest>getOutgoingRequests(@Param("user") String userName);

    @Query("delete from FriendsRequest fr where fr.friendUser = :friend and fr.requestingUser = :requester or fr.friendUser = :requester and fr.requestingUser = :friend")
    void deleteRequest(@Param("friend")User friend, @Param("requester") User requester);
}
