package com.anf.model.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/** Entity class for a message */
@Entity
@Table(name = "private_messages")
public class PrivateMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  int message_id;

  /** Message itself */
  @ManyToOne
  @JoinColumn(name = "receiver", nullable = false)
  private User receiver;

  @ManyToOne
  @JoinColumn(name = "sender", nullable = false)
  private User sender;

  @Column(name = "sending_time", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date sendingDate;

  @Column(name = "message", columnDefinition = "TEXT", nullable = false)
  private String message;

  @Column(name = "is_read", nullable = false)
  private boolean isRead;

  public PrivateMessage(int key, String text) {
    this.message_id = key;
    this.message = text;
  }

  public int getMessage_id() {
    return message_id;
  }

  public void setMessage_id(int message_id) {
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

  public PrivateMessage(User receiver, User sender) {
    this.sendingDate = new Date();
    this.receiver = receiver;
    this.sender = sender;
  }

  public PrivateMessage() {}

  public User getReceiver() {
    return receiver;
  }

  public void setReceiver(User receiver) {
    this.receiver = receiver;
  }

  public User getSender() {
    return sender;
  }

  public void setSender(User sender) {
    this.sender = sender;
  }

  public Date getSendingDate() {
    return sendingDate;
  }

  public void setSendingDate(Date sendingDate) {
    this.sendingDate = sendingDate;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isRead() {
    return isRead;
  }

  public void setRead(boolean read) {
    isRead = read;
  }

  @Override
  public String toString() {
    try {
      return new ObjectMapper().writeValueAsString(this);
    } catch (JacksonException e) {
      return "{}";
    }
    //        return "{" +
    //                "\"message_id\":" + message_id +
    //                ", \"receiver\":" + receiver.getLogin() +
    //                ", \"sender\":" + sender.getLogin() +
    //                ", \"sendingDate\":" + sendingDate +
    //                ", \"message\":\"" + message + '\"' +
    //                ", \"isRead\":" + isRead +
    //                '}';
  }
}
