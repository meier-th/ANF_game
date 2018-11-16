package com.p3212.EntityClasses;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class FriendsCompositeKey implements Serializable {
  
    @ManyToOne
    @JoinColumn(name="user1")
    User user1;

    @ManyToOne
    @JoinColumn(name="user2")
    User user2;
    
    public FriendsCompositeKey(User us1, User us2) {
        this.user1 = us1;
        this.user2 = us2;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof FriendsCompositeKey)) return false;
        return (Objects.deepEquals(o, this));
    }

    @Override
    public int hashCode() {
        return Objects.hash(user1, user2);
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }
    
    
    
}
