import dao.UserDao;
import domain.Level;
import domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserServiceTest {

    // 테스트 대상인 UserService 빈을 제공받을 수 있도록 @Autowired 가 붙은 인스턴스 변수로 선언해준다.
    @Autowired
    UserService userService;

    @Autowired
    UserDao userDao;

    List<User> users; // 픽스쳐

    @Before
    public void setUp() {
        users = Arrays.asList(
                new User("aaa", "박범진", "p1", Level.BASIC, 49, 0),
                new User("bbb", "강성명", "p2", Level.BASIC, 50, 0),
                new User("ccc", "신승환", "p3", Level.SILVER, 60, 29),
                new User("ddd", "이상호", "p4", Level.SILVER, 60, 30),
                new User("eee", "오민규", "p5", Level.GOLD, 100, 100)
        );
    }

    // 빈 설정이 잘 되었는지 테스트
    @Test
    public void bean() {
        assertThat(this.userService, is(notNullValue()));
    }

    // 사용자 레벨 업그레이드 테스트
    @Test
    public void upgradeLevels() {
        userDao.deleteAll();
        for(User user : users) userDao.add(user);

        userService.upgradeLevels();

        // 각 사용자별로 업그레이드 후의 예상 레벨을 검증한다.
        checkLevel(users.get(0), Level.BASIC);
        checkLevel(users.get(1), Level.SILVER);
        checkLevel(users.get(2), Level.SILVER);
        checkLevel(users.get(3), Level.GOLD);
        checkLevel(users.get(4), Level.GOLD);
    }

    private void checkLevel(User user, Level expectedLevel) {
        // DB 에서 사용자 정보를 가져와 레벨을 확인하는 코드가 중복되므로 헬퍼 메소드로 분리했다.
        User userUpdate = userDao.get(user.getId());
        assertThat(userUpdate.getLevel(), is(expectedLevel));
    }
}
