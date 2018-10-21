package P3212.ANFBackend.JDBCImplementations;

import P3212.ANFBackend.AnfBackendApplication;
import P3212.ANFBackend.DAOs.DAOAppearanceInterface;
import P3212.ANFBackend.EntityClasses.Appearance;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AppearanceJDBCImplementation extends JdbcDaoSupport implements DAOAppearanceInterface {
    @Override
    public void create(int id, Appearance.Gender gender, Appearance.SkinColour skinColour, Appearance.HairColour hairColour, Appearance.ClothesColour clothesColour) {
        String sql = "insert into APPEARANCE values (?, ?, ?, ?)";
        getJdbcTemplate().update(sql, gender.toString(), skinColour.toString(), hairColour.toString(), clothesColour.toString());
    }

    @Override
    public void update(int id, Appearance.Gender gender, Appearance.SkinColour skinColour, Appearance.HairColour hairColour, Appearance.ClothesColour clothesColour) {
        String sql = "update APPEARANCE set gender = ?, skinColour = ?, hairColour = ?, clothesColour = ? where id = ?";
        getJdbcTemplate().update(sql, gender.toString(), skinColour.toString(), hairColour.toString(), clothesColour.toString());
    }

    @Override
    public Appearance get(int id) {
        String sql = "select * from APPEARANCE whre id = ?";
        return getJdbcTemplate().queryForObject(sql, new Object[]{id}, new AppearanceMapper());
    }

    @Override
    public void delete(int id) {
        String sql = "delete from Appearance where id = ?";
        getJdbcTemplate().update(sql, id);
    }

    private class AppearanceMapper implements RowMapper<Appearance> {
        @Override
        public Appearance mapRow(ResultSet rs, int rowNum) throws SQLException {
            Appearance appear = (Appearance)AnfBackendApplication.cont.getBean("Appearance");
            appear.setClothesColour(Appearance.ClothesColour.valueOf(rs.getString("clothesColour")));
            appear.setGender(Appearance.Gender.valueOf(rs.getString("Gender")));
            appear.setHairColour(Appearance.HairColour.valueOf(rs.getString("hairColour")));
            appear.setSkinColour(Appearance.SkinColour.valueOf(rs.getString("skinColour")));
            return appear;
        }
    }
}
