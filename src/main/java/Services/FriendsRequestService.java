package Services;

import EntityClasses.FriendsRequest;
import Repositories.FriendsRequestRepository;
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
    void removeRequest(int id) {
        repository.deleteById(id);
    }
}
