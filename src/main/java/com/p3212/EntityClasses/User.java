package com.p3212.EntityClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.io.Serializable;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * Represents User entity. Used to operate on users' registration data
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    /**
     * User's login
     */
    @Id
    @Column(length=30)
    private String login;

    /**
     * User's in-game character
     */
    @OneToOne
    @JoinColumn(name = "character_id")
    private Character character;

    /**
     * User's password
     */
    private String password;

    /**
     * User's stats object
     */
    @OneToOne
    @JoinColumn(name = "stats_id")
    private Stats stats;

    @OneToMany(mappedBy = "message_id.sender")
    @JsonIgnore
    private List<PrivateMessage> outgoingMessages;

    @OneToMany(mappedBy = "message_id.receiver")
    @JsonIgnore
    private List<PrivateMessage> incomingMessages;

    @OneToMany(mappedBy = "friends_id.user1")
    @JsonIgnore
    private List<Friends> friends1;

    @OneToMany(mappedBy = "request_id.friendUser")
    @JsonIgnore
    private List<FriendsRequest> friendRequestsIn;

    @OneToMany(mappedBy = "request_id.requestingUser")
    @JsonIgnore
    private List<FriendsRequest> friendRequestOut;

    @OneToMany(mappedBy = "friends_id.user2")
    @JsonIgnore
    private List<Friends> friends2;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "login"), inverseJoinColumns = @JoinColumn(name = "role"))
    private Set<Role> roles;

    public User() {}

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    public List<FriendsRequest> getFriendRequestsIn() {
        return friendRequestsIn;
    }

    public void setFriendRequestsIn(List<FriendsRequest> friendRequestsIn) {
        this.friendRequestsIn = friendRequestsIn;
    }

    public List<FriendsRequest> getFriendRequestOut() {
        return friendRequestOut;
    }

    public void setFriendRequestOut(List<FriendsRequest> friendRequestOut) {
        this.friendRequestOut = friendRequestOut;
    }

    public List<Friends> getFriends1() {
        return friends1;
    }

    public void setFriends1(List<Friends> friends1) {
        this.friends1 = friends1;
    }

    public List<Friends> getFriends2() {
        return friends2;
    }

    public void setFriends2(List<Friends> friends2) {
        this.friends2 = friends2;
    }

    public List<PrivateMessage> getOutgoingMessages() {
        return outgoingMessages;
    }

    public void setOutgoingMessages(List<PrivateMessage> outgoingMessages) {
        this.outgoingMessages = outgoingMessages;
    }

    public List<PrivateMessage> getIncomingMessages() {
        return incomingMessages;
    }

    public void setIncomingMessages(List<PrivateMessage> incomingMessages) {
        this.incomingMessages = incomingMessages;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    /**
     * Getter {@link User#password}
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Setter {@link User#password}
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter {@link User#login}
     */
    public String getLogin() {
        return login;
    }

    /**
     * Setter {@link User#login}
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Getter {@link User#character}
     */
    public Character getCharacter() {
        return character;
    }

    /**
     * Setter {@link User#character}
     */
    public void setCharacter(Character character) {
        this.character = character;
    }

}
