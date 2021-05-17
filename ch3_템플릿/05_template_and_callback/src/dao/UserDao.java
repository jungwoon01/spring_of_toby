package dao;


import domain.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    private DataSource dataSource; // db 연결 클래스
    private JdbcTemplate jdbcTemplate; // JdbcTemplate

    // 수정자 메소드이면서 JdbcContext 에 대한 생성, DI 작업을 동시에 수행한다.
    public void setDataSource(DataSource dataSource) {
        // JdbcTemplate 초기화
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        this.dataSource = dataSource;
    }

    // jdbcTemplate 을 사용하는 add 메서드
    public void add(User user) throws SQLException {
        this.jdbcTemplate.update("insert into user(id, name, password) values(?,?,?)", user.getId(), user.getName(), user.getPassword());
    }

    public User get(String id) throws SQLException {

        Connection con = dataSource.getConnection();

        // 쿼리 준비
        PreparedStatement ps = con.prepareStatement("select * from user where id = ?");
        ps.setString(1, id);

        // executeQuery() 는 쿼리를 실행한 결과 set 을 return 한다.
        ResultSet rs = ps.executeQuery();
        User user = null;
        if (rs.next()) {
            user = new User();
            user.setId(rs.getString("id"));
            user.setName(rs.getString("name"));
            user.setPassword(rs.getString("password"));
        }

        // 연결 끊기
        rs.close();
        ps.close();
        con.close();

        if (user == null)
            throw new EmptyResultDataAccessException(1);

        return user;
    }

    // JdbcTemplate 의 update 메서드를 사용하는 deleteAll()
    public void deleteAll() throws SQLException {
        this.jdbcTemplate.update("delete from user");
    }

    // JdbcTemplate query() 를 이용해 만든 getCount()
    /*public int getCount() throws SQLException {
        return this.jdbcTemplate.query(new PreparedStatementCreator() {
            // prepareStatement 객체를 템플릿으로 리턴
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                return connection.prepareStatement("select count(*) from user");
            }
        }, new ResultSetExtractor<Integer>() {
            // 쿼리에 대한 result 를 받을 수 있고 리턴받을 타입도 지정할 수 있다.
            @Override
            public Integer extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                resultSet.next();
                return resultSet.getInt(1);
            }
        });
    }*/

    // JdbcTemplate queryForInt() 를 이용해 만든 getCount()
    // 리턴 타입이 int 인 쿼리만 적어주면 된다.
    public int getCount() throws SQLException {
        return this.jdbcTemplate.queryForInt("select count(*) from user");
    }
}
