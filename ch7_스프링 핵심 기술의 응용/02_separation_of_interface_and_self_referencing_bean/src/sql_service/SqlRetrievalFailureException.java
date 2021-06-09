package sql_service;

public class SqlRetrievalFailureException extends RuntimeException{
    public SqlRetrievalFailureException(String message) {
        super(message);
    }

    public SqlRetrievalFailureException(String message, Throwable cause) {
        // SQL 을 가져오는 데 실패한 근본 원인을 담을 수 있도록 중첩 예외를 저장할 수 있는 생성자를 만들어둔다.
        super(message, cause);
    }
}
