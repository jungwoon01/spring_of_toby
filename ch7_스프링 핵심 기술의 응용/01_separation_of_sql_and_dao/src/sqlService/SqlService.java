package sqlService;

public interface SqlService {
    String getSql(String key) throws SqlRetrievalFailureException; // 런타임 예외이브로 특별히 복구해야 할 필요가 없다면 무시해도 된다.
}
