package EntityClasses;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class MessageCompositeKey implements Serializable {
    @ManyToOne
    @JoinColumn(name="receiver")
    User receiver;

    @ManyToOne
    @JoinColumn(name="sender")
    User sender;
    
    @Column(name="message")
    String message;
    
    @Column(name= "sending_time")
    @Temporal(TemporalType.TIMESTAMP)
    Date sendingDate; 
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof FriendsCompositeKey)) return false;
        return (Objects.deepEquals(o, this));
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiver, sender);
    }
}
