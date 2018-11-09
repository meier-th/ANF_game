package EntityClasses;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name = "friends")
public class Friends implements Serializable {

    @EmbeddedId
    public FriendsCompositeKey friends_id;
}
