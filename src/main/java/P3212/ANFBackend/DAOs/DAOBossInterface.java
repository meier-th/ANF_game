package P3212.ANFBackend.DAOs;

import P3212.ANFBackend.EntityClasses.Boss;

import java.util.List;

/**
 * CRUD interface for 'Bidju'
 * Uses a mapper - implementation of a RowMapper<> interface for ORM purpose
 */
public interface DAOBossInterface {
    /**
     * Returns bidju with obtained ID
     *
     * @param id The ID of the bidju
     * @return Needed bidju
     */
    Boss get(int id);

    /**
     * Returns all bidju's in a list
     *
     * @return List of bidju's
     */
    List<Boss> list();

}
