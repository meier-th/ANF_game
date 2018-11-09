package EntityClasses;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "friend_request")
public class FriendsRequest {

    @EmbeddedId
    public FriendRequestCompositeKey request_id;
}
