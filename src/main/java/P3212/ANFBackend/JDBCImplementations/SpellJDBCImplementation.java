package P3212.ANFBackend.JDBCImplementations;

import P3212.ANFBackend.AnfBackendApplication;
import P3212.ANFBackend.DAOs.DAOSpellInterface;
import P3212.ANFBackend.EntityClasses.Spell;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Implementation of DAOSpellInterface
 * Look for methods documentation there
 */
public class SpellJDBCImplementation extends JdbcDaoSupport implements DAOSpellInterface {

    @Override
    public Spell getSpell(int id) {
        String sql = "select * from TECHNIQUES where id = ?";
        Spell spell = this.getJdbcTemplate().queryForObject(sql, new Object[]{id}, new SpellMapper());
        return spell;
    }

    @Override
    public Spell getSpell(String name) {
        String sql = "select * from TECHNIQUES where Name = ?";
        Spell spell = this.getJdbcTemplate().queryForObject(sql, new Object[]{name}, new SpellMapper());
        return spell;
    }

    @Override
    public List<Spell> listSpells() {
        String sql = "select * from TECHNIQUES";
        List<Spell> spells = this.getJdbcTemplate().query(sql, new SpellMapper());
        return spells;
    }
    
    public static class SpellMapper implements RowMapper<Spell> {

        @Override
        public Spell mapRow(ResultSet rs, int i) throws SQLException {
            Spell spell = (Spell)AnfBackendApplication.cont.getBean("Spell");
            spell.setBaseChakraConsumption(rs.getInt("Chakra_consumption"));
            spell.setBaseDamage(rs.getInt("BaseDamage"));
            spell.setChakraConsumptionPerLevel(rs.getInt("chakraPerLevel"));
            spell.setDamagePerLevel(rs.getInt("damagePerLevel"));
            spell.setDescription(rs.getString("Description"));
            spell.setName(rs.getString("Name"));
            spell.setId(rs.getInt("ID"));
            return spell;
        }
        
    }
    
}
