package dao;


import domain.User;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // StatementStrategy stmt : 클라이언트가 호출할 때 넘겨줄 전략 파라미터
    public void jdbcContextWithStatementStrategy(StatementStrategy stmt){
        Connection con = null;
        PreparedStatement ps = null;

        try {
            // db connection 가져오기
            con = dataSource.getConnection();

            // statement 적용
            ps = stmt.makePreparedStatement(con);

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(ps != null){ try { ps.close(); } catch (SQLException throwables) { } }
            if (con != null) { try { con.close(); } catch (SQLException throwables) { } }
        }
    }

    public void add(final User user){

        jdbcContextWithStatementStrategy(new StatementStrategy() {

            @Override
            public PreparedStatement makePreparedStatement(Connection con) throws SQLException {

                // 쿼리 준비
                PreparedStatement ps = con.prepareStatement("insert into user (id, name, password) value (?,?,?)");
                ps.setString(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getPassword());

                return ps;
            }
        });
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
        jdbcContextWithStatementStrategy(new StatementStrategy() {
            @Override
            public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
                PreparedStatement ps;
                ps = c.prepareStatement("delete from user");
                return ps;
            }
        });
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
