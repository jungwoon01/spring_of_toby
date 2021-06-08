package dao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// UserDao 에 있던 jdbcContextWithStatementStrategy()를 다른 DAO 클래스에서 사용할 수 있게 독립시키기 위한 클래스
// DataSource 와는 달리 구체 클래스다.
// 독립적인 JDBC 컨텍스트를 제공해주는 서비스 오브젝트고 구현 방법이 바뀔 가능성이 없어서
// 따로 인터페이스를 구현하도록 만들지 않았다.
public class JdbcContext {

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 메서드 사용시 전략만 주입하면 어디서든지 편하게 사용할 수 있다.
    public void workWithStatementStrategy(StatementStrategy stmt) throws SQLException {
        Connection c = null;
        PreparedStatement ps = null;

        try {
            c = this.dataSource.getConnection();
            ps = stmt.makePreparedStatement(c);

            ps.executeUpdate();
        } catch (SQLException e){
            throw e;
        }finally {
            if (ps != null) { try { ps.close(); } catch (SQLException e ) { e.printStackTrace(); }}
            if (c != null) { try { c.close(); } catch (SQLException e ) { e.printStackTrace(); }}
        }
    }

    public void executeSql(final String query) throws SQLException {
        workWithStatementStrategy(
                new StatementStrategy() {
                    // 변하지 않는 콜백 클래스 정의와 오브젝트 생성
                    @Override
                    public PreparedStatement makePreparedStatement(Connection con) throws SQLException {
                        return con.prepareStatement(query);
                    }
                }
        );
    }
}
