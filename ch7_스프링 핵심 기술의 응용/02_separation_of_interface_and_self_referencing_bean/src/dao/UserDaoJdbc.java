package dao;


import domain.Level;
import domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import sql_service.SqlService;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDaoJdbc implements UserDao {

    private JdbcTemplate jdbcTemplate; // JdbcTemplate

    private SqlService sqlService;

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    // 수정자 메소드이면서 JdbcTemplate 에 대한 생성, DI 작업을 동시에 수행한다.
    public void setDataSource(DataSource dataSource) {
        // JdbcTemplate 초기화
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 재사용 가능하도록 독립시킨 RowMapper
    private RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet resultSet, int i) throws SQLException {
            User user = new User();
            user.setId(resultSet.getString("id"));
            user.setName(resultSet.getString("name"));
            user.setPassword(resultSet.getString("password"));
            user.setLevel(Level.valueOf(resultSet.getInt("level")));
            user.setLogin(resultSet.getInt("login"));
            user.setRecommend(resultSet.getInt("recommend"));
            user.setEmail(resultSet.getString("email"));
            return user;
        }
    };

    public void add(User user) {
        this.jdbcTemplate.update(
                sqlService.getSql("userAdd"), // sql 을 제거하고 외부에서 주입 받는다.
                user.getId(), user.getName(), user.getPassword(), user.getLevel().intValue(),
                user.getLogin(), user.getRecommend(), user.getEmail()
        );
    }

    // id 값을 가진 row 를 가져오는 메서드
    public User get(String id) {
        return this.jdbcTemplate.queryForObject(sqlService.getSql("userGet"),new Object[] {id} ,userRowMapper);
    }

    // JdbcTemplate 의 update 메서드를 사용하는 deleteAll()
    public void deleteAll(){
        this.jdbcTemplate.update(sqlService.getSql("userDeleteAll"));
    }

    // JdbcTemplate queryForInt() 를 이용해 만든 getCount()
// 리턴 타입이 int 인 쿼리만 적어주면 된다.
    public int getCount(){
        return this.jdbcTemplate.queryForInt(sqlService.getSql("userGetCount"));
    }

    // User 테이블의 모든 row 를 가져오는 메서드
    public List<User> getAll() {
        return this.jdbcTemplate.query(sqlService.getSql("userGetAll"), userRowMapper);
    }

    // User 테이블 수정 메서드
    public void update(User user) {
        this.jdbcTemplate.update(
                sqlService.getSql("userUpdate"),
                user.getName(), user.getPassword(), user.getLevel().intValue(), user.getLogin(), user.getRecommend(), user.getEmail(),
                user.getId()
        );
    }
}
