package EntityClasses;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
    @GeneratedValue
    int id;
    /**
     * Message itself
     */
    
    @Column(name = "message")
    String message;
    /**
     * Sender of the message
     */
    @ManyToOne
    @JoinColumn(name = "sender")
    User sender;
    /**
     * Receiver of the message
     */
    @ManyToOne
    @JoinColumn(name = "receiver")
    User receiver;
    
    private boolean idRead;
}
