package EntityClasses;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "friend_request")
public class FriendsRequest {

    @Id
    @GeneratedValue
    int id;

    @ManyToOne
    @JoinColumn(name = "requestingUser")
    User requestingUser;

    @ManyToOne
    @JoinColumn(name = "friendUser")
    User friendUser;
}
