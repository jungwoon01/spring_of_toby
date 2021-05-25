package test;

import dao.UserDao;
import domain.Level;
import domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static service.UserService.MIN_LOG_COUNT_FOR_SILVER;
import static service.UserService.MIN_RECOMMEND_FOR_GOLD;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserServiceTest {

    // 테스트 대상인 UserService 빈을 제공받을 수 있도록 @Autowired 가 붙은 인스턴스 변수로 선언해준다.
    @Autowired
    UserService userService;

    @Autowired
    UserDao userDao;

    @Autowired
    PlatformTransactionManager transactionManager;

    List<User> users; // 픽스쳐

    @Before
    public void setUp() {
        users = Arrays.asList(
                new User("aaa", "박범진", "p1", Level.BASIC, MIN_LOG_COUNT_FOR_SILVER-1, 0),
                new User("bbb", "강성명", "p2", Level.BASIC, MIN_LOG_COUNT_FOR_SILVER, 0),
                new User("ccc", "신승환", "p3", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD-1),
                new User("ddd", "이상호", "p4", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD),
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
    public void upgradeLevels(){
        userDao.deleteAll();
        for(User user : users) userDao.add(user);

        userService.upgradeLevels();

        // 다음 레벨로 업그레이드될 것인가 아닌가를 지정한다.
        checkLevel(users.get(0), false);
        checkLevel(users.get(1), true);
        checkLevel(users.get(2), false);
        checkLevel(users.get(3), true);
        checkLevel(users.get(4), false);
    }

    @Test
    public void add() {
        userDao.deleteAll();

        User userWithLevel = users.get(4); // GOLD 레벨 레벨이 이지 지정된 User 라면 레벨을 초기화하지 않아야 한다.
        // 레벨이 비어 있는 사용자. 로직에 따라 등록 중에 BASIC 레벨로 설정되야 한다.
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        // 디비에 정보를 저장한다.
        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        // DB에 저장된 결과를 가져와 확인한다.
        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel())); // 레벨이 있는 유저는 본인의 레벨 그대로
        assertThat(userWithoutLevel.getLevel(), is(Level.BASIC)); // 레벨 없는 유저는 BASIC 으로 초기화
    }

    private void checkLevel(User user, boolean upgraded) {
        User userUpdate = userDao.get(user.getId());
        if (upgraded) {
            assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel())); // 다음 레벨이 무엇인지는 Level 에게 물어보면 된다.
        }
        else {
            assertThat(userUpdate.getLevel(), is(user.getLevel())); // 업그레이드가 일어나지 않았는지 확인
        }
    }

    // 예외 발생시 작업 취소 여부 테스트
    @Test
    public void upgradeAllOrNothing(){
        // 예외를 발생시킬 네 번째 사용자의 id를 넣어서 테스트 용 UserService 대역 오브젝트를 생성한다.
        UserService testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(this.userDao); // userDao 수동 주입
        testUserService.setTransactionManager(transactionManager); // userService 빈의 프로퍼티 설정과 동일한 수동 DI

        userDao.deleteAll();
        for(User user : users) userDao.add(user);

        try {
            testUserService.upgradeLevels();
            fail("TestUserServiceException expected"); // TestUserService 는 업그레이드 작업 중에 예외가 발생해야한다.
        }
        catch (TestUserServiceException e) { // 예외를 잡아서 계속 진행되도록 한다. 그 외의 예외라면 테스트 실패

        }

        // 예외가 발생하기 전에 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 돌아왔나 확인
        checkLevel(users.get(1), false);
    }

    // 테스트를 위한 UserService 상속 받은 static 클래스
    static class TestUserService extends UserService {
        private String id;

        private TestUserService(String id) { // 예외를 발생시킬 id 지정
            this.id = id;
        }

        @Override
        protected void upgradeLevel(User user) {
            // 지정된 id의 User 오브젝트가 발견되면 예외를 던져서 작업을 강제로 중단.
            if (user.getId().equals(this.id)) throw new TestUserServiceException();
            super.upgradeLevel(user);
        }
    }

    // 런타임예외 클래스
    static class TestUserServiceException extends RuntimeException {
    }
}
