package P3212.ANFBackend.DAOs;

import P3212.ANFBackend.EntityClasses.Boss;
import P3212.ANFBackend.EntityClasses.UserInFight;
import java.util.List;
import P3212.ANFBackend.EntityClasses.Character;
/**
 * CRUD interface for Fighting-related information
 * Uses a mapper - implementation of a RowMapper<> interface for ORM purpose
 */

public interface DAOUserInFightInterface {
    
    /**
     * Returns a List containing information about User's fighting history
     * @param charctr - character
     * @return A List of UserInFight objects
     */
    public List<UserInFight> listUserInFight(Character charctr);
    
    /**
     * Adds a new PVP fight record in database
     * @param first - first Fighter
     * @param second - second Fighter
     * @param winner - winner
     * @param ratingChange - The amount of Rating that one user lost and another obtained
     * @param date - the date of fight
     */
    public void addPvpFight(Character first, Character  second, Character winner, int ratingChange, java.time.LocalDate date);
    
    /**
     * Adds a new Boss fight record in database
     * @param boss - Boss id
     * @param date - date of fight
     * @return Returns id of a fight to be used in 'addParticipationInFight' method
     */
    public int addMonsterFight(Boss boss, java.time.LocalDate date);
    
    /**
     * Adds a new record in ParticipatingInFight table in database
     * @param fighter - Fighter
     * @param result - Result of a fight for Fighter
     * @param ratingCh - The amount of Rating user lost or obtained
     * @param fight_id - The id of a fight
     */
    public void addParticipationInFight(Character fighter, UserInFight.Result result, int ratingCh, int fight_id);
    
}
