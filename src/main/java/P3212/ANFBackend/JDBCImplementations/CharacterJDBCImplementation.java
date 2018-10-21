package P3212.ANFBackend.JDBCImplementations;

import P3212.ANFBackend.AnfBackendApplication;
import P3212.ANFBackend.DAOs.DAOCharacterInterface;
import P3212.ANFBackend.EntityClasses.Appearance;
import P3212.ANFBackend.EntityClasses.Character;
import P3212.ANFBackend.EntityClasses.NinjaAnimalRace;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CharacterJDBCImplementation extends JdbcDaoSupport implements DAOCharacterInterface {
    @Override
    public void create(int id, LocalDate date, float resistance, int hp, int damage, int chakra, NinjaAnimalRace race) {
        String sql = "insert into Persons values(?, ?, ?, ?, ?, ?, ?)";
        getJdbcTemplate().update(sql, id, date, hp, damage, resistance, chakra, race);
    }

    @Override
    public void delete(int id) {
        String sql = "delete from PERSONS where ID = ?";
        getJdbcTemplate().update(sql, id);
    }

    @Override
    public void update(float resistance, int hp, int damage, int chakra) {

    }

    @Override
    public Character get(int id) {
        String sql = "select * from PERSONS where ID = ?";
        return getJdbcTemplate().queryForObject(sql, new Object[]{id}, new CharacterMapper());
    }

    @Override
    public List<Character> list() {
        String sql = "select * from PERSONS";
        return getJdbcTemplate().query(sql, new CharacterMapper());
    }

    /**
     * ORM for {@link Character}
     * makes Character object from query result
     */
    private class CharacterMapper implements RowMapper<Character> {
        @Override
        public Character mapRow(ResultSet rs, int rowNum) throws SQLException {
            Character character = (Character)AnfBackendApplication.cont.getBean("Character");
            character.setAnimalRace(NinjaAnimalRace.valueOf(rs.getInt("Ninja_animal_race_ID")));
            character.setId(rs.getInt("ID"));
            character.setMaxChakraAmount(rs.getInt("Chakra_amount"));
            character.setMaxHP(rs.getInt("maxHP"));
            character.setPhysicalDamage(rs.getInt("physicalDamage"));
            character.setResistance(rs.getFloat("resistance"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'yyyy-MM-dd'");
            String dateStr = rs.getString("Date_of_birth");
            character.setCreationDate(LocalDate.parse(dateStr, formatter));
            AppearanceJDBCImplementation impl = (AppearanceJDBCImplementation)AnfBackendApplication.cont.getBean("AppearanceJDBC");
            Appearance appear = impl.get(rs.getInt("Appearance_ID"));
            character.setAppearance(appear);
            return character;
        }
    }
}
