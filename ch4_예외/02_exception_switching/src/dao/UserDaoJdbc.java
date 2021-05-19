package dao;


import domain.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDaoJdbc implements UserDao {

    private JdbcTemplate jdbcTemplate; // JdbcTemplate

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
            return user;
        }
    };

    // jdbcTemplate 을 사용하는 add 메서드
    public void add(User user){
        this.jdbcTemplate.update("insert into user(id, name, password) values(?,?,?)", user.getId(), user.getName(), user.getPassword());
    }

    // id 값을 가진 row 를 가져오는 메서드
    public User get(String id) {
        return this.jdbcTemplate.queryForObject("select * from user where id = ?",new Object[] {id} ,userRowMapper);
    }

    // JdbcTemplate 의 update 메서드를 사용하는 deleteAll()
    public void deleteAll(){
        this.jdbcTemplate.update("delete from user");
    }

    // JdbcTemplate queryForInt() 를 이용해 만든 getCount()
    // 리턴 타입이 int 인 쿼리만 적어주면 된다.
    public int getCount(){
        return this.jdbcTemplate.queryForInt("select count(*) from user");
    }

    // User 테이블의 모든 row 를 가져오는 메서드
    public List<User> getAll() {
        return this.jdbcTemplate.query("select * from user order by id", userRowMapper);
    }
}
