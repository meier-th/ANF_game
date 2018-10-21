package P3212.ANFBackend.JDBCImplementations;

import P3212.ANFBackend.AnfBackendApplication;
import P3212.ANFBackend.DAOs.DAONinjaAnimalInterface;
import P3212.ANFBackend.EntityClasses.NinjaAnimal;
import P3212.ANFBackend.EntityClasses.NinjaAnimalRace;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class NinjaAnimalJDBCImplementation extends JdbcDaoSupport implements DAONinjaAnimalInterface {

    @Override
    public void create(int id, String name, NinjaAnimalRace race, int damage, int hp, int reqlevel) {
        String sql = "insert into NINJA_ANIMALS values(?, ?, ?, ?, ?, ?)";
        getJdbcTemplate().update(sql, id, name, race, reqlevel, hp, damage);
    }

    @Override
    public void update(int id, int damage, int hp, int reqlevel) {
        String sql = "update NINJA_ANIMALS set damage = ?, hp = ?, reqlevel = ? where id = ?";
        getJdbcTemplate().update(sql, damage, hp, reqlevel, id);
    }

    @Override
    public NinjaAnimal get(int id) {
        String sql = "select * from NINJA_ANIMALS where ID = ?";
        return getJdbcTemplate().queryForObject(sql, new Object[]{id}, new NinjaAnimalMapper());
    }

    @Override
    public List<NinjaAnimal> list() {
        String sql = "select * from NINJA_ANIMALS";
        return getJdbcTemplate().query(sql, new NinjaAnimalMapper());
    }

    private class NinjaAnimalMapper implements RowMapper<NinjaAnimal> {
        @Override
        public NinjaAnimal mapRow(ResultSet rs, int rowNum) throws SQLException {
            NinjaAnimal ninjAn = (NinjaAnimal)AnfBackendApplication.cont.getBean("NinjaAnimal");
            ninjAn.setDamage(rs.getInt("damage"));
            ninjAn.setHp(rs.getInt("HP"));
            ninjAn.setId(rs.getInt("ID"));
            ninjAn.setName(rs.getString("Name"));
            ninjAn.setRequiredLevel(rs.getInt("RequiredLevel"));
            ninjAn.setRace(NinjaAnimalRace.valueOf(rs.getInt("Ninja_animal_race_ID")));
            return ninjAn;
        }
    }
}
