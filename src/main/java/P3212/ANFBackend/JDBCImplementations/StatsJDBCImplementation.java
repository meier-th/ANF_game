package P3212.ANFBackend.JDBCImplementations;

import P3212.ANFBackend.AnfBackendApplication;
import P3212.ANFBackend.DAOs.DAOStatsInterface;
import P3212.ANFBackend.EntityClasses.Stats;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Implementation of DAOStatsInterface
 * Look for methods documentation there
 */
public class StatsJDBCImplementation extends JdbcDaoSupport implements DAOStatsInterface{    
    
    protected StatsJDBCImplementation(){}
    
    @Override
    public void create(String owner, int rating, int fights, int wins, int losses, int deaths) {
        String sql = "insert into stats values (?, ?, ?, ?, ?, ?)";
        this.getJdbcTemplate().update(sql, owner, rating, fights, wins, losses, deaths);
    }

    @Override
    public Stats get(String login) {
        String sql = "select * from stats where owner = ?";
        Stats stats = this.getJdbcTemplate().queryForObject(sql, new Object[]{login}, new StatsMapper());
        return stats;
    }

    @Override
    public List<Stats> getTop(int number) {
        String sql = "select * from stats order by rating limit ?";
        List<Stats>toRet = this.getJdbcTemplate().query(sql, new Object[] {number}, new StatsMapper());
        return toRet;
    }
    
    @Override
    public void updateStats(String login, int rating, int fights, int wins, int losses, int deaths) {
        String sql = "update stats set rating = ?, fights = ?, wins = ?, losses = ?, deaths = ? where login = ?";
        this.getJdbcTemplate().update(sql, login, rating, fights, wins, losses, deaths);
    }

    @Override
    public void delete(String login) {
        String sql = "delete from stats where owner = ?";
        this.getJdbcTemplate().update(sql, login);
    }

    @Override
    public List<Stats> listStats() {
        String sql = "select * from stats";
        List<Stats> list = this.getJdbcTemplate().query(sql, new StatsMapper());
        return list;
    }
    
    private static class StatsMapper implements RowMapper<Stats> {

    @Override
    public Stats mapRow(ResultSet rs, int i) throws SQLException {
        Stats stats = (Stats)AnfBackendApplication.cont.getBean("Stats");
        stats.setRating(rs.getInt("rating"));
        stats.setFights(rs.getInt("fights"));
        stats.setWins(rs.getInt("wins"));
        stats.setLosses(rs.getInt("losses"));
        stats.setDeaths(rs.getInt("deaths"));
        return stats;
    }
    
}
    
}
