package P3212.ANFBackend.DAOs;

import P3212.ANFBackend.EntityClasses.*;
import java.util.List;
/**
     *  CRUD interface for 'Stats'
     *  Uses a mapper - implementation of a RowMapper<> interface for ORM purpose 
     */
public interface DAOStatsInterface {
    
    /**
     * Creates a new stats record in the table
     * @param owner - user's login
     * @param rating - rating value
     * @param fights - number of fights
     * @param wins - number of wins
     * @param losses - number of losses
     * @param deaths number of deaths
     */
  public void create(String owner, int rating, int fights, int wins, int losses, int deaths);
  

  /**
   * Gets a stats object given its login
   * @param login - user's login
   * @return - user object
   */
  public Stats get(String login);
  
  /**
   * Gets top stats sorted by rating
   * @param number - number of stats to retrieve
   * @return A list of stats objects
   */
  public List<Stats> getTop(int number);
  
  /**
   * Updates the stats record given the users login
   * @param login - user's login
   * @param rating - new rating value
   * @param fights - new fights number
   * @param wins - new wins number
   * @param losses - new losses number
   * @param deaths - new deaths number
   */
  public void updateStats(String login, int rating, int fights, int wins, int losses, int deaths);
  
  /**
   * Gets all the stats records from database
   * @return a list of stats objects
   */
  public List<Stats> listStats();

 /**
  * Deletes a stats record from the table given the users login
  * @param login - user's login
  */
  public void delete(String login);
}
