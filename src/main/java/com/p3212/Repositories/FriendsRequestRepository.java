package com.p3212.Repositories;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.p3212.EntityClasses.FriendsRequest;
import com.p3212.EntityClasses.User;
import org.springframework.data.jpa.repository.Modifying;

@Repository
public interface FriendsRequestRepository extends CrudRepository<FriendsRequest, Integer> {
    
    @Query("select r from FriendsRequest r where r.friendUser = :user")
    List<FriendsRequest>getIncomingRequests(@Param("user") User user);
    
    @Query("select r from FriendsRequest r where r.requestingUser = :user")
    List<FriendsRequest>getOutgoingRequests(@Param("user") User user);

    @Modifying
    @Query("delete from FriendsRequest fr where fr.friendUser = :friend and fr.requestingUser = :requester or fr.friendUser = :requester and fr.requestingUser = :friend")
    void deleteRequest(@Param("friend")User friend, @Param("requester") User requester);
}
