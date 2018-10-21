package P3212.ANFBackend.JDBCImplementations;

import P3212.ANFBackend.AnfBackendApplication;
import P3212.ANFBackend.DAOs.DAOSpellHandlingInterface;
import P3212.ANFBackend.EntityClasses.Spell;
import P3212.ANFBackend.EntityClasses.SpellHandling;
import java.util.List;
import P3212.ANFBackend.EntityClasses.Character;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Implementation of a DAOSpellHandlingInterface interface
 * Uses a mapper - implementation of a RowMapper<> interface for ORM purpose 
 */
public class SpellHandlingJDBCImplementation extends JdbcDaoSupport implements DAOSpellHandlingInterface {

    @Override
    public List<SpellHandling> getByCharacter(Character character) {
        String sql = "select * from USERS_OF_TECHNIQUES where User_ID = ?";
        List<SpellHandling> hndlList = this.getJdbcTemplate().query(sql, new Object[]{character.getId()}, new SpellHandlingMapper());
        return hndlList;
    }

    @Override
    public SpellHandling getSpellHandlingOfCharacter(Character character, Spell spell) {
        String sql = "select * from USERS_OF_TECHNIQUES where User_ID = ?, Technique_ID = ?";
        SpellHandling toRet = this.getJdbcTemplate().queryForObject(sql, new Object[]{character.getId(), spell.getId()}, new SpellHandlingMapper());
        return toRet;
    }

    @Override
    public List<SpellHandling> getAllSpellHandling() {
        String sql = "select * from USERS_OF_TECHNIQUES";
        List<SpellHandling> toRet = this.getJdbcTemplate().query(sql, new SpellHandlingMapper());
        return toRet;
    }

    @Override
    public void createSpellHandling(Character character, Spell spell) {
        String sql = "insert into USERS_OF_TECHNIQUES values (?, ?, ?)";
        this.getJdbcTemplate().update(sql, new Object[]{character.getId(), spell.getId(), 1});
    }

    @Override
    public void deleteSpellHandling(Character character, Spell spell) {  
        String sql = "delete from USERS_OF_TECHNIQUES where User_ID = ?, Technique_ID = ?";
        this.getJdbcTemplate().update(sql, new Object[]{character.getId(), spell.getId()});
    }

    @Override
    public void updateSpellHandling(Character character, Spell spell, int level) {
        String sql = "update USERS_OF_TECHNIQUES set Spell_level = ? where User_ID = ?, Technique_ID = ?";
        this.getJdbcTemplate().update(sql, new Object[]{level, character.getId(), spell.getId()});
    }

    private static class SpellHandlingMapper implements RowMapper<SpellHandling> {

        @Override
        public SpellHandling mapRow(ResultSet rs, int i) throws SQLException {
            SpellHandling spellhandl = (SpellHandling)AnfBackendApplication.cont.getBean("SpellHandling");
            CharacterJDBCImplementation charGttr = (CharacterJDBCImplementation)AnfBackendApplication.cont.getBean("CharacterJDBC");
            SpellJDBCImplementation spellGttr = (SpellJDBCImplementation)AnfBackendApplication.cont.getBean("SpellJDBC");
            Spell spl = spellGttr.getSpell(rs.getInt("Technique_ID"));
            Character character = charGttr.get(rs.getInt("User_ID"));
            spellhandl.setCharacter(character);
            spellhandl.setSpell(spl);
            spellhandl.setSpellLevel(rs.getInt("Spell_level"));
            return spellhandl;
        }
        
    }
    
}
