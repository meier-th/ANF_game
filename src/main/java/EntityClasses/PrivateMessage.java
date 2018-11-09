package EntityClasses;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Entity class for a message
 */
@Entity
@Table(name = "PrivateMessages")
public class PrivateMessage {

    @EmbeddedId
    MessageCompositeKey message_id;
    /**
     * Message itself
     */
    
    
    
    boolean isRead;
}
