package EntityClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
     * Represents User entity. Used to operate on users' registration data 
     */

@Entity
@Table(name="users")
public class User implements Serializable {
    
    /**
     * User's login
     */
    @Id
    private String login;
    
    /**
     * User's in-game character
     */
    
    @OneToOne
    @JoinColumn(name = "character_id")
    private Character character;
    
    /**
     * User's email
     */
    private String email;
    
    /**
     * User's password
     */
    private String password;
    
    /**
     * User's stats object
     */
    
    @OneToMany(mappedBy="sender")
    @JsonIgnore
    private List<PrivateMessage> outgoingMessages;
    
    @OneToMany(mappedBy="receiver")
    @JsonIgnore
    private List<PrivateMessage> incomingMessages;
    
    @OneToMany(mappedBy="user1")
    @JsonIgnore
    private List<Friends>friends1;
    
    @OneToMany(mappedBy="friendUser")
    @JsonIgnore
    private List<FriendsRequest> friendRequestsIn;
    
    @OneToMany(mappedBy="requestingUser")
    @JsonIgnore
    private List<FriendsRequest> friendRequestOut;
    
    @OneToMany(mappedBy="user2")
    @JsonIgnore
    private List<Friends>friends2;
    
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

	@OneToOne
    @JoinColumn(name="stats_id")
    private Stats stats; //Dependency injected with Autowire by name
    
    public Stats getStats() {
		return stats;
	}

	public void setStats(Stats stats) {
		this.stats = stats;
	}

	/**
     * Default constructor, to be used for dependency injection
     */
    public User(){}
    
    /**
     * To be used when retrieved from database
     * @param login user's login
     * @param email user's email
     * @param password user's password
     */
    public User(String login, String email, String password) {
        this.login = login;
        this.password = password;
        this.email = email;
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
    
    /**Getter
     * {@link User#login}
     */
    public String getUsername(){
        return this.login;
    }
    
    /**Getter
     * {@link User#email}
     */
    public String getEmail() {
        return this.email;
    }
    
    /**Getter
     * {@link User#password}
     */
    public String getPassword() {
        return this.password;
    }
    
    /**Setter
     * {@link User#login}
     */
    public void setUsername(String login){
        this.login = login;
    }
    
    /**Setter
     * {@link User#email}
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**Setter
     * {@link User#password}
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**Setter
     * {@link User#stats}
     */
    //public void setStats(Stats st) {
    //    this.stats = st;
    //}
    
    /**Setter
     * {@link User#stats}
     */
    //public Stats getStats() {
    //    return this.stats;
   // }

    /**
     * Getter
     * {@link User#login} 
     */
    public String getLogin() {
        return login;
    }
    
    /**
     * Setter
     * {@link User#login} 
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Getter
     * {@link User#character} 
     */    
    public Character getCharacter() {
        return character;
    }

    /**
     * Setter
     * {@link User#character} 
     */    
    public void setCharacter(Character character) {
        this.character = character;
    }
    
    
    
}
