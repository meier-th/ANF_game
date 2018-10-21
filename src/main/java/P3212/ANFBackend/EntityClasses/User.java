package P3212.ANFBackend.EntityClasses;

import org.springframework.beans.factory.annotation.Autowired;
    /**
     * Represents User entity. Used to operate on users' registration data 
     */
public class User {

    /**
     * User's login
     */
    private String login;
    
    /**
     * User's in-game character
     */
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
    @Autowired
    private Stats stats; //Dependency injected with Autowire by name
    
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
    public void setStats(Stats st) {
        this.stats = st;
    }
    
    /**Setter
     * {@link User#stats}
     */
    public Stats getStats() {
        return this.stats;
    }

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
