package dao;


import domain.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

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

    public void add(final User user) throws SQLException {


        this.jdbcContext.workWithStatementStrategy(
                new StatementStrategy() {
                    @Override
                    public PreparedStatement makePreparedStatement(Connection con) throws SQLException {
                        // 쿼리 준비
                        PreparedStatement ps = con.prepareStatement("insert into user (id, name, password) value (?,?,?)");
                        ps.setString(1, user.getId());
                        ps.setString(2, user.getName());
                        ps.setString(3, user.getPassword());

                        return ps;
                    }
                }
        );
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

    public void deleteAll() throws SQLException {
        this.jdbcContext.executeSql("delete from user");
    }

    public int getCount() throws SQLException {
        Connection con = dataSource.getConnection();

        PreparedStatement ps = con.prepareStatement("select count(*) from user");

        ResultSet rs = ps.executeQuery();
        rs.next();
        int count = rs.getInt(1);

        rs.close();
        ps.close();
        con.close();

        return count;
    }
}
