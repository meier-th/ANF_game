package com.p3212.EntityClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.io.Serializable;
import java.util.Set;
import javax.persistence.*;

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

    @OneToMany(mappedBy = "message_id.sender", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PrivateMessage> outgoingMessages;

    @OneToMany(mappedBy = "message_id.receiver", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PrivateMessage> incomingMessages;

    @OneToMany(mappedBy = "request_id.friendUser", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<FriendsRequest> friendRequestsIn;

    @OneToMany(mappedBy = "request_id.requestingUser", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<FriendsRequest> friendRequestOut;

    @JoinTable(name = "friends", joinColumns = {
            @JoinColumn(name = "user1", referencedColumnName = "login", nullable = false)}, inverseJoinColumns = {
            @JoinColumn(name = "user2", referencedColumnName = "login", nullable = false)})
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<User> friends;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "login"), inverseJoinColumns = @JoinColumn(name = "role"))
    private Set<Role> roles;

    public User() {
    }

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

    public List<User> getFriends() {
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
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
