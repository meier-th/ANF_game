package Services;

import EntityClasses.FriendRequestCompositeKey;
import EntityClasses.FriendsRequest;
import EntityClasses.User;
import Repositories.FriendsRequestRepository;
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
    void addRequest(FriendsRequest request) {
        repository.save(request);
    }

    /**
     * Remove request (when accepted or denied)
     *
     * @param id id of the request
     */
    void removeRequest(FriendRequestCompositeKey id) {
        repository.deleteById(id);
    }
    
    ArrayList<User> requestingUsers(User user) {
        String name = user.getLogin();
        List<FriendsRequest> requests = repository.getIncomingRequests(name);
        ArrayList<User>requesters = new ArrayList<User>();
        for (FriendsRequest req : requests) {
            requesters.add(req.request_id.getRequestingUser());
        }
        return requesters;
    }
    
    ArrayList<User> requestedUsers(User user) {
        String name = user.getLogin();
        List<FriendsRequest> requests = repository.getOutgoingRequests(name);
        ArrayList<User>requesters = new ArrayList<User>();
        for (FriendsRequest req : requests) {
            requesters.add(req.request_id.getFriendUser());
        }
        return requesters;
    }
    
}