package P3212.ANFBackend.JDBCImplementations;

import P3212.ANFBackend.AnfBackendApplication;
import P3212.ANFBackend.DAOs.DAOUserInterface;
import P3212.ANFBackend.EntityClasses.Stats;
import P3212.ANFBackend.EntityClasses.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Implementation of DAOUserInterface
 * Look for methods documentation there
 */
public class UserJDBCImplementation extends JdbcDaoSupport implements DAOUserInterface{

    protected UserJDBCImplementation(){}
    
    @Override
    public void create(String login, String password, String email) {
        String sql = "insert into users (login, password, email) values (?, ?, ?)";
        this.getJdbcTemplate().update(sql, login, password, email);        
    }

    @Override
    public void create(String login, String password) {
        String sql = "insert into users (login, password) values (?, ?)";
        this.getJdbcTemplate().update(sql, login, password);
    }

    @Override
    public User get(String login) {
        String sql = "select * from users where login = ?";
        User user = this.getJdbcTemplate().queryForObject(sql, new Object[]{login}, new UserMapper());
        return user;
    }

    @Override
    public User getByEmail(String email) {
        String sql = "select * from users where email = ?";
        User user = this.getJdbcTemplate().queryForObject(sql, new Object[]{email}, new UserMapper());
        return user;
    }

    @Override
    public List<User> listUsers() {
        String sql = "select * from users";
        List<User> users = this.getJdbcTemplate().query(sql, new UserMapper());
        return users;
    }

    @Override
    public void deleteByEmail(String email) {
        String sql = "delete from users where email = ?";
        this.getJdbcTemplate().update(sql, email);
    }

    @Override
    public void delete(String login) {
        String sql = "delete from users where login = ?";
        this.getJdbcTemplate().update(sql, login);
    }

    private static class UserMapper implements RowMapper<User>{
    
    /**
     *  ORM for 'User'
     */
    
    
    @Override
    public User mapRow(ResultSet  rs, int rowNum) throws SQLException {
        User user = (User)AnfBackendApplication.cont.getBean("User");
        String email = rs.getString("email");
        if (email != null)
            user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setPassword(rs.getString("password"));
        StatsJDBCImplementation statsGetter = (StatsJDBCImplementation)AnfBackendApplication.cont.getBean("StatsJDBC");
        CharacterJDBCImplementation charGetter = (CharacterJDBCImplementation)AnfBackendApplication.cont.getBean("CharacterJDBC");
        P3212.ANFBackend.EntityClasses.Character ch = charGetter.get(rs.getInt("Character_id"));
        user.setCharacter(ch);
        String login = user.getUsername();
        Stats st = statsGetter.get(login);
        user.setStats(st);
        return user;
    }
  
}
    
}
