package P3212.ANFBackend.JDBCImplementations;

import P3212.ANFBackend.AnfBackendApplication;
import P3212.ANFBackend.DAOs.DAOBossInterface;
import P3212.ANFBackend.EntityClasses.Boss;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of DAOBossInterface
 */
public class BossJDBCImplementation extends JdbcDaoSupport implements DAOBossInterface {

    @Override
    public Boss get(int id) {
        String sql = "select * from BIDJU where ID = ?";
        return getJdbcTemplate().queryForObject(sql, new Object[]{id}, new BossMapper());

    }

    @Override
    public List<Boss> list() {
        String sql = "select * from BIDJU";
        return  getJdbcTemplate().query(sql, new BossMapper());
    }

    private class BossMapper implements RowMapper<Boss> {
        @Override
        public Boss mapRow(ResultSet rs, int rowNum) throws SQLException {
            Boss boss = (Boss)AnfBackendApplication.cont.getBean("Boss");
            boss.setId(rs.getInt("ID"));
            boss.setMaxChakraAmount(rs.getInt("Chakra_amount"));
            boss.setName(rs.getString("Name"));
            boss.setNumberOfTails(rs.getInt("Amount_of_tails"));
            return boss;
        }
    }
}
