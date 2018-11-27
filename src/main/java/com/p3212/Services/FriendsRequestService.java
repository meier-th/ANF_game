package com.p3212.Services;

import com.p3212.EntityClasses.FriendsRequest;
import com.p3212.EntityClasses.User;
import com.p3212.Repositories.FriendsRequestRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for friend requests
 */
@Service
public class FriendsRequestService {
    /**
     * Repository for requests
     */
    @Autowired
    FriendsRequestRepository repository;

    /**
     * Add a request to friends
     *
     * @param request A @link{FriendsRequest} object to add
     */
    public void addRequest(FriendsRequest request) {
        repository.save(request);
    }

    /**
     * Remove request (when accepted or denied)
     *
     * @param id id of the request
     */
    public void removeRequest(User friend, User requester) {
        repository.deleteRequest(friend, requester);
    }
    
    public ArrayList<User> requestingUsers(User user) {
        List<FriendsRequest> requests = repository.getIncomingRequests(user);
        ArrayList<User>requesters = new ArrayList<>();
        requests.forEach((req) -> {
            requesters.add(req.getRequestingUser());
        });
        return requesters;
    }
    
    public ArrayList<User> requestedUsers(User user) {
        List<FriendsRequest> requests = repository.getOutgoingRequests(user);
        ArrayList<User>requesters = new ArrayList<>();
        requests.forEach((req) -> {
            requesters.add(req.getFriendUser());
        });
        return requesters;
    }
    
}