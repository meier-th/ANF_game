package com.p3212.EntityClasses;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


public class MessageCompositeKey implements Serializable {
    @ManyToOne
    @JoinColumn(name = "receiver")
    User receiver;

    @ManyToOne
    @JoinColumn(name = "sender")
    User sender;


    @Column(name = "sending_time")
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

    public Date getSendingDate() {
        return sendingDate;
    }


    public MessageCompositeKey(User sender, User receiver) {
        this.receiver = receiver;
        this.sender = sender;
        this.sendingDate = new Date();
    }

    public MessageCompositeKey(User sender, User receiver, Date date) {
        this.receiver = receiver;
        this.sender = sender;
        this.sendingDate = date;
    }

    public MessageCompositeKey() {
    }

}
