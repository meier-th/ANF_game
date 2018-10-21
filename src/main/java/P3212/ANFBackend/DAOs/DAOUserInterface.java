package P3212.ANFBackend.DAOs;

    import java.util.List;
    import P3212.ANFBackend.EntityClasses.*;
/**
     * CRUD interface for User
     * Uses a mapper - implementation of a RowMapper<> interface for ORM purpose
     */
public interface DAOUserInterface {
    
    /**
     * Creates a user record with parameters passed:
     * @param login user's login
     * @param password user's password
     * @param email user's emial
     */
  public void create(String login, String password, String email);
  
  /**
   * Creates a user record with email = null
   * @param login user's login
   * @param password user's password
   */
  public void create(String login, String password);
  
  /**
   * Gets a user with the login given
   * @param login user's login
   * @return the user object
   */
  public User get(String login);
  
  /**
   * Gets a user with email given
   * @param email - user's email
   * @return the user object
   */
  public User getByEmail(String email);
  
  /**
   * Gets all the users from the users table
   * @return a list of user objects
   */
  public List<User> listUsers();
  
  /**
   * Deletes a user record with a email given
   * @param email user's email
   */
  public void deleteByEmail(String email);
  
  /**
   * Deletes a User record with a login given
   * @param login user's login
   */
  public void delete(String login);
  
}
