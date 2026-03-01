package com.anf.model.database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "friend_requests")
public class FriendsRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public int request_id;

  @ManyToOne
  @JoinColumn(name = "friend_request", nullable = false)
  User friendUser;

  @ManyToOne
  @JoinColumn(name = "requesting_user", nullable = false)
  User requestingUser;

  public FriendsRequest(User sender, User receiver) {
    this.friendUser = receiver;
    this.requestingUser = sender;
  }

  public FriendsRequest() {}

  public int getRequest_id() {
    return request_id;
  }

  public void setRequest_id(int request_id) {
    this.request_id = request_id;
  }

  public User getFriendUser() {
    return friendUser;
  }

  public void setFriendUser(User friendUser) {
    this.friendUser = friendUser;
  }

  public User getRequestingUser() {
    return requestingUser;
  }

  public void setRequestingUser(User requestingUser) {
    this.requestingUser = requestingUser;
  }

  @Override
  public String toString() {
    return "{"
        + "\"friendUser\": "
        + "\""
        + friendUser.getLogin()
        + "\","
        + "\"requestingUser\": "
        + "\""
        + requestingUser.getLogin()
        + "\", \"request_id\": "
        + request_id
        + "}";
  }
}
