package EntityClasses;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class FriendRequestCompositeKey implements Serializable {
    @ManyToOne
    @JoinColumn(name="friend_request")
    User friendUser;

    @ManyToOne
    @JoinColumn(name="requesting_user")
    User requestingUser;
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof FriendsCompositeKey)) return false;
        return (Objects.deepEquals(o, this));
    }

    @Override
    public int hashCode() {
        return Objects.hash(friendUser, requestingUser);
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
