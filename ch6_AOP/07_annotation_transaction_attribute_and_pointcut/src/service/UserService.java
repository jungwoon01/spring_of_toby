package service;

import domain.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional // <tx:method name="*" /> 과 같은 설정 효과를 가져온다
public interface UserService {
    void add(User user);

    // <tx:method name="get" read-only="true"/>를 애노테이션 방식으로 변경한다.
    // 메소드 단위로 부여된 트랜잭션의 속성이 타임 레벨에 부여된것에 우선해서 적용된다.
    // 같은 속성을 가졌어도 메소드 레벨에 부여될 때는 메소드마다 반복될 수밖에 없다.
    @Transactional(readOnly = true)
    User get(String id);
    @Transactional(readOnly = true)
    List<User> getAll();

    void deleteAll();
    void update(User user);
    void upgradeLevels();
}
