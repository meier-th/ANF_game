package com.p3212.EntityClasses;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "friend_request")
public class FriendsRequest {

    @EmbeddedId
    public FriendRequestCompositeKey request_id;
    
    public FriendsRequest (FriendRequestCompositeKey key) {
        this.request_id = key;
    }
    
    public FriendsRequest(){}
    
}
