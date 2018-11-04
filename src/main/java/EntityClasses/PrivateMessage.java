package EntityClasses;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity class for a message
 */
@Entity
@Table(name = "PrivateMessages")
public class PrivateMessage {

    /**
     * Id of the message
     */
    @Id
    int id;
    /**
     * Message itself
     */
    @Column(name = "message")
    String message;
    /**
     * Sender of the message
     */
    @Column(name = "sender")
    User sender;
    /**
     * Receiver of the message
     */
    @Column(name = "receiver")
    User receiver;

    @Column(name = "isRead")
    boolean isRead;
}
