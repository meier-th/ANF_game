package com.anf.service;

import com.anf.model.FriendsRequest;
import com.anf.model.User;
import com.anf.repository.FriendsRequestRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/** Service for friend requests */
@Service
@AllArgsConstructor
public class FriendsRequestService {
  /** Repository for requests */
  private final FriendsRequestRepository repository;

  /**
   * Add a request to friends
   *
   * @param request A @link{FriendsRequest} object to add
   */
  public void addRequest(FriendsRequest request) {
    repository.save(request);
  }

  public Optional<FriendsRequest> getRequest(int id) {
    return repository.findById(id);
  }

  /**
   * Remove request (when accepted or denied)
   *
   * @param id id of the request
   */
  @Transactional
  public void removeRequest(User friend, User requester) {
    repository.deleteRequest(friend, requester);
  }

  public void removeById(int id) {
    repository.deleteById(id);
  }

  public ArrayList<User> requestingUsers(User user) {
    List<FriendsRequest> requests = repository.getIncomingRequests(user);
    ArrayList<User> requesters = new ArrayList<>();
    requests.forEach(
        (req) -> {
          requesters.add(req.getRequestingUser());
        });
    return requesters;
  }

  public ArrayList<User> requestedUsers(User user) {
    List<FriendsRequest> requests = repository.getOutgoingRequests(user);
    ArrayList<User> requesters = new ArrayList<>();
    requests.forEach(
        (req) -> {
          requesters.add(req.getFriendUser());
        });
    return requesters;
  }
}
