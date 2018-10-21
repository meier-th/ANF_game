package P3212.ANFBackend.JDBCImplementations;

import P3212.ANFBackend.AnfBackendApplication;
import P3212.ANFBackend.DAOs.DAOUserInFightInterface;
import P3212.ANFBackend.EntityClasses.Boss;
import P3212.ANFBackend.EntityClasses.Character;
import P3212.ANFBackend.EntityClasses.UserInFight;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Implementation of DAOUserInFightInterface
 * Look for methods documentation there
 */

public class UserInFightJDBCImplementation extends JdbcDaoSupport implements DAOUserInFightInterface {

    @Override
    public List<UserInFight> listUserInFight(Character charctr) {
        String sql = "select * from FIGHT_PVP where Person1_ID = ?";
        List<UserInFight> listOne = this.getJdbcTemplate().query(sql, new Object[] {charctr.getId()}, new firstMapper());
        sql = "select * from FIGHT_PVP where Person2_ID = ?";
        List<UserInFight> listTwo = this.getJdbcTemplate().query(sql, new Object[] {charctr.getId()}, new secondMapper());
        sql = "select ParticipatingInFight.Result, ParticipatingInFight.Rating_change, FIGHT_VS_AI.AI_ID, FIGHT_VS_AI.DateOfFight from ParticipatingInFight inner join FIGHT_VS_AI on (ParticipatingInFight.FIGHT_ID = FIGHT_VS_AI.ID) where ParticipatingInFight.Person_ID = ?";
        List<UserInFight> listThree = this.getJdbcTemplate().query(sql, new Object[] {charctr.getId()}, new thirdMapper() );
        listOne.addAll(listTwo);
        listOne.addAll(listThree);
        return listOne;
    }

    @Override
    public void addPvpFight(Character first, Character second, Character winner, int ratingChange, LocalDate date) {
        String sql = "insert into FIGHT_PVP values (?, ?, ?, ?, ?)";
        this.getJdbcTemplate().update(sql, new Object[]{first.getId(), second.getId(), date.format(DateTimeFormatter.ISO_DATE), winner.getId(), ratingChange});
    }

    @Override
    public int addMonsterFight(Boss boss, LocalDate date) {
        String sql = "insert into FIGHT_VS_AI (AI_ID, DateOfFight) values (? ,?)";
        this.getJdbcTemplate().update(sql, new Object[] {boss.getId(), date.format(DateTimeFormatter.ISO_DATE)});
        Integer id = this.getJdbcTemplate().queryForObject("select max(ID) from FIGHT_VS_AI", Integer.class);
        return id.intValue();
    }

    @Override
    public void addParticipationInFight(Character fighter, UserInFight.Result result, int ratingCh, int fight_id) {
        String sql = "insert into ParticipatingInFight values (?, ?, ?, ?)";
        this.getJdbcTemplate().update(sql, new Object[]{fight_id, fighter.getId(), result.toString(), ratingCh});
    }
    
    private static class firstMapper implements RowMapper<UserInFight> {

        @Override
        public UserInFight mapRow(ResultSet rs, int i) throws SQLException {
            UserInFight us = (UserInFight)AnfBackendApplication.cont.getBean("UserInFight");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'yyyy-MM-dd'");
            String dateStr = rs.getString("DateOfFight");
            us.setDate(LocalDate.parse(dateStr, formatter));
            int winner = rs.getInt("Winner_ID");
            int self = rs.getInt("Person1_ID");
            if (winner == self) {
                us.setResult(UserInFight.Result.WON);
            } else 
                us.setResult(UserInFight.Result.LOST);
            us.setRatingChange(rs.getInt("rating_change"));
            CharacterJDBCImplementation impl = (CharacterJDBCImplementation)AnfBackendApplication.cont.getBean("CharacterJDBC");
            Character rival = impl.get(rs.getInt("Person2_ID"));
            us.setRival(rival);
            return us;
        }
        
    }
    private static class secondMapper implements RowMapper<UserInFight> {

        @Override
        public UserInFight mapRow(ResultSet rs, int i) throws SQLException {
            UserInFight us = (UserInFight)AnfBackendApplication.cont.getBean("UserInFight");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'yyyy-MM-dd'");
            String dateStr = rs.getString("DateOfFight");
            us.setDate(LocalDate.parse(dateStr, formatter));
            int winner = rs.getInt("Winner_ID");
            int self = rs.getInt("Person2_ID");
            if (winner == self) {
                us.setResult(UserInFight.Result.WON);
            } else 
                us.setResult(UserInFight.Result.LOST);
            us.setRatingChange(rs.getInt("rating_change"));
            CharacterJDBCImplementation impl = (CharacterJDBCImplementation)AnfBackendApplication.cont.getBean("CharacterJDBC");
            Character rival = impl.get(rs.getInt("Person1_ID"));
            us.setRival(rival);
            return us;
        }
        
    }
    
    private static class thirdMapper implements RowMapper<UserInFight> {

        @Override
        public UserInFight mapRow(ResultSet rs, int i) throws SQLException {
            UserInFight us = (UserInFight)AnfBackendApplication.cont.getBean("UserInFight");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'yyyy-MM-dd'");
            String dateStr = rs.getString("DateOfFight");
            us.setDate(LocalDate.parse(dateStr, formatter));
            us.setRatingChange(rs.getInt("Rating_change"));
            String result = rs.getString("Result");
            switch (result) {
                case "LOST": {
                    us.setResult(UserInFight.Result.LOST);
                    break;
                }
                case "WON": {
                    us.setResult(UserInFight.Result.WON);
                    break;
                }
                case "DIED": {
                    us.setResult(UserInFight.Result.DIED);
                    break;
                }
            }
            BossJDBCImplementation bossGetter = (BossJDBCImplementation)AnfBackendApplication.cont.getBean("BossJDBC");
            Boss boss = bossGetter.get(rs.getInt("AI_ID"));
            us.setRival(boss);
            return us;
        }
    }
}
