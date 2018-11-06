package EntityClasses;

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
    
}
