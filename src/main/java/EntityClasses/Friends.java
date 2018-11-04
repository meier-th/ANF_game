package EntityClasses;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Friends")
public class Friends {

    @Id
    User user1;

    @Id
    User user2;
}
