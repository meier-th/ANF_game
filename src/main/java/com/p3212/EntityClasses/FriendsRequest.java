package com.p3212.EntityClasses;

import javax.persistence.*;

@Entity
@Table(name = "friend_request")
public class FriendsRequest {

    @Id
    public int request_id;

    @ManyToOne
    @JoinColumn(name = "friend_request")
    User friendUser;

    @ManyToOne
    @JoinColumn(name = "requesting_user")
    User requestingUser;

    public FriendsRequest(int key) {
        this.request_id = key;
    }

    public FriendsRequest() {
    }

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
}
