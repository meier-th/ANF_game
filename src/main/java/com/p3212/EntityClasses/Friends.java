package com.p3212.EntityClasses;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name = "friends")
public class Friends implements Serializable {

    @EmbeddedId
    public FriendsCompositeKey friends_id;
    
    public Friends(FriendsCompositeKey key) {
        this.friends_id = key;
    }

    public FriendsCompositeKey getFriends_id() {
        return friends_id;
    }
    
    public Friends(){}
    
}
