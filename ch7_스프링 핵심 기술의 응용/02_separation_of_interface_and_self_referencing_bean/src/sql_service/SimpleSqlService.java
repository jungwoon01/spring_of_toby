package sql_service;

import java.util.Map;

public class SimpleSqlService implements SqlService{
    private Map<String, String> sqlMap;

    public void setSqlMap(Map<String, String> sqlMap) {
        this.sqlMap = sqlMap;
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        String sql = sqlMap.get(key); // 내부 SqlMap 에서 SQL 을 가져온다.
        if (sql == null) {
            // 인터페이스에 정의된 규약대로 SQL 을 가져오는 데 실패하면 예외를 던지게 한다.
            String msg = key.concat(" 에 대한 SQL 을 찾을 수 없습니다.");
            throw new SqlRetrievalFailureException(msg);
        } else {
            return sql;
        }
    }
}
