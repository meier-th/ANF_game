package com.p3212.EntityClasses;

import javax.persistence.Column;
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
    
    @Column(name="message", columnDefinition="TEXT")
    String message;
    
    boolean isRead;
    
    public PrivateMessage(MessageCompositeKey key, String text) {
        this.message_id = key;
        this.message = text;
    }

    public MessageCompositeKey getMessage_id() {
        return message_id;
    }

    public void setMessage_id(MessageCompositeKey message_id) {
        this.message_id = message_id;
    }

    public boolean isIsRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public String getMessage() {
        return message;
    }
    
    public PrivateMessage(){}
    
}
