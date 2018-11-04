package EntityClasses;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Friend_Request")
public class FriendsRequest {

    @Id
    int id;

    @Column(name = "requestingUser")
    User requestingUser;

    @Column(name = "friendUser")
    User friendUser;
}
