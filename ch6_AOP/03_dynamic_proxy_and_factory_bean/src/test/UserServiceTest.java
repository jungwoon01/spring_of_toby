package test;

import dao.UserDao;
import domain.Level;
import domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import service.TransactionHandler;
import service.TxProxyFactoryBean;
import service.UserService;
import service.UserServiceImpl;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static service.UserServiceImpl.MIN_LOG_COUNT_FOR_SILVER;
import static service.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserServiceTest {

    // @Autowired 는 기본적으로 타입을 이용해 빈을 찾는다.
    // 같은 타입이 여러개 있을땐 필드 이름으로 빈을 검색한다.
    // 빈에 설정된 userService 는 UserServiceTx 클래스를 빈으로 등록해놓았다.
    @Autowired
    UserService userService;

    // MockMailSender 설정해 주기 위해 사용.
    @Autowired
    UserServiceImpl userServiceImpl;

    @Autowired
    UserDao userDao;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    MailSender mailSender;

    @Autowired
    ApplicationContext context;

    List<User> users; // 픽스쳐

    @Before
    public void setUp() {
        users = Arrays.asList(
                new User("aaa", "박범진", "p1", Level.BASIC, MIN_LOG_COUNT_FOR_SILVER-1, 0, "useradmin1@naver.com"),
                new User("bbb", "강성명", "p2", Level.BASIC, MIN_LOG_COUNT_FOR_SILVER, 0, "useradmin2@naver.com"),
                new User("ccc", "신승환", "p3", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD-1, "useradmin3@naver.com"),
                new User("ddd", "이상호", "p4", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD, "useradmin4@naver.com"),
                new User("eee", "오민규", "p5", Level.GOLD, 100, 100, "useradmin5@naver.com")
        );
    }

    // 빈 설정이 잘 되었는지 테스트
    @Test
    public void bean() {
        assertThat(this.userService, is(notNullValue()));
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

        assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel())); // 레벨이 있는 유저는 본인의 레벨 그대로
        assertThat(userWithoutLevel.getLevel(), is(Level.BASIC)); // 레벨 없는 유저는 BASIC 으로 초기화
    }

    @Test
    public void upgradeLevels() throws Exception {
        // 고립된 테스트에서는 테스트 대상 오브젝트를 직접 생성하면 된다.
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        // 목 오브젝트로 만든 UserDao 를 직접 DI 해준다.
        MockUserDao mockUserDao = new MockUserDao(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        // 메일 발송 결과를 테스트할 수 있도록 목 오브젝트를 만들어 userService 의 의존 오브젝트로 주입해둔다.
        MockMailSender mockMailSender = new MockMailSender();
        userServiceImpl.setMailSender(mockMailSender);

        // 업그레이드 테스트, 메일 발송이 일어나면 MockMailSender 오브젝트의 리스트에 그 결과가 저장된다.
        userServiceImpl.upgradeLevels();

        List<User> updated = mockUserDao.getUpdated(); // MockUserDao 로부터 업데이트 결과를 가져온다.
        assertThat(updated.size(), is(2));
        checkUserAndLevel(updated.get(0), "bbb", Level.SILVER);
        checkUserAndLevel(updated.get(1), "ddd", Level.GOLD);

        // 목 오브젝트에 저장된 메일 수신자 목록을 가져와 업그레이드 대상과 일치하는지 확인한다.
        List<String> request = mockMailSender.getRequests();
        assertThat(request.size(), is(2));
        assertThat(request.get(0), is(users.get(1).getEmail()));
        assertThat(request.get(1), is(users.get(3).getEmail()));
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
    @DirtiesContext // 컨택스트 무효화 애노테이션
    public void upgradeAllOrNothing() throws Exception {
        // 예외를 발생시킬 네 번째 사용자의 id를 넣어서 테스트 용 UserService 대역 오브젝트를 생성한다.
        UserServiceImpl testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(this.userDao); // userDao 수동 주입
        testUserService.setMailSender(mailSender); // MailSender 수동 DI

        // 팩토리 빈 자체를 가져와야 하므로 빈 이름에 &를 넣어준다.
        TxProxyFactoryBean txProxyFactoryBean = context.getBean("&userService", TxProxyFactoryBean.class);
        txProxyFactoryBean.setTarget(testUserService);
        // 변경된 타깃 설정을 이용해서 트랜잭션 다이나믹 프록시 오브젝트를 다시 생성한다.
        UserService txUserService = (UserService) txProxyFactoryBean.getObject();

        userDao.deleteAll();
        for(User user : users) userDao.add(user);


        try {
            // 트랜잭션 기능을 분리한 오브젝트를 통해 예외 발생용 TestUserService 가 호출되게 해야 한다.
            txUserService.upgradeLevels();
            fail("TestUserServiceException expected"); // TestUserService 는 업그레이드 작업 중에 예외가 발생해야한다.
        }
        catch (TestUserServiceException e) { // 예외를 잡아서 계속 진행되도록 한다. 그 외의 예외라면 테스트 실패

        }

        // 예외가 발생하기 전에 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 돌아왔나 확인
        checkLevel(users.get(1), false);
    }

    @Test
    public void mockUpgradeLevels() throws Exception {
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        // 다이내믹한 목 오브젝트 생성과 메소드의 리턴 값 설정, 그리고 DI 세 줄이면 충분하다.
        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        // 리턴 값이 없는 메소드를 가진 목 오브젝트는 더욱 간단하게 만들 수 있다.
         MailSender mockMailSender = mock(MailSender.class);
         userServiceImpl.setMailSender(mockMailSender);

        // 업그레이드 테스트, 메일 발송이 일어나면 MockMailSender 오브젝트의 리스트에 그 결과가 저장된다.
        userServiceImpl.upgradeLevels();

        // 목 오브젝트가 제공하는 검증 기능을 통해서 어떤 메소드가 몇 번 호출 됐는지,
        // 파라미터는 무엇인지 확인할 수 있다.
        verify(mockUserDao, times(2)).update(any(User.class)); // any()를 사용하면 파미터를 무시하고 호출 횟수만 확인할 수 있다.
        verify(mockUserDao).update(users.get(1)); // users.get(1)을 파라미터로 update()가 호출된 적이 있는지를 확인해준다.
        assertThat(users.get(1).getLevel(), is(Level.SILVER));
        verify(mockUserDao).update(users.get(3)); // users.get(3)을 파라미터로 update()가 호출된 적이 있는지를 확인해준다.
        assertThat(users.get(3).getLevel(), is(Level.GOLD));

        // ArgumentCaptor 라는 것을 사용해서 실제 MailSender 목 오브젝트에 전달된 파라미터를 가져와 내용을 검증하는 방법을 사용했다.
        // 파라미터를 직접 비교하기보다는 파라미터의 내부 정보를 확인해야 하는 경우에 유용하다.
        ArgumentCaptor<SimpleMailMessage> mailMassageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);
        // 파라미터를 정밀하게 검사하기 위해 캡처할 수도 있다.
        verify(mockMailSender, times(2)).send(mailMassageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMassageArg.getAllValues();
        assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
        assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));
    }

    public void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
        assertThat(updated.getId(), is(expectedId));
        assertThat(updated.getLevel(), is(expectedLevel));
    }

    // 테스트를 위한 UserService 상속 받은 static 클래스
    static class TestUserService extends UserServiceImpl {
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

    // 목 오브젝트로 만든 메일 전송 확인용 클래스
    static class MockMailSender implements MailSender {
        // UserService 로부터 전송 요청을 받을 메일 주소를 저장해두고 이를 읽을 수 있게 한다.
        private List<String> requests = new ArrayList<String>();

        public List<String> getRequests() {
            return requests;
        }

        @Override
        public void send(SimpleMailMessage simpleMailMessage) throws MailException {
            // 전송 요청을 받은 이메일 주소를 저장해둔다.
            // 간단하게 첫번째 수신자 메일 주소만 저장했다.(어차피 한번에 하나의 메일주소만 보낼 것이다.)
            // 중복 호출까지 포함해서 다 저장해둔다.
            requests.add(simpleMailMessage.getTo()[0]);
        }

        @Override
        public void send(SimpleMailMessage[] simpleMailMessages) throws MailException {

        }
    }

    static class MockUserDao implements UserDao {
        private List<User> users; // 레벨 업그레이드 후보 User 오브젝트 목록
        private List<User> updated = new ArrayList<>(); // 업그레이드 대상 오브젝트를 저장해둘 목록

        private MockUserDao(List<User> users) {
            this.users = users;
        }

        public List<User> getUpdated() {
            return this.updated;
        }

        // 스텁 기능 제공
        @Override
        public List<User> getAll() {
            return this.users;
        }

        // 목 오브젝트 기능 제공
        @Override
        public void update(User user) {
            updated.add(user);
        }

        // 테스트에 사용되지 않는 메소드들
        // 사용하지 않는 메소드들은 실수로 사용하는 경우를 대비해서 UnsupportedOperationException 을 던지도록 만드는 편이 좋다
        @Override
        public void add(User user) { throw new UnsupportedOperationException(); }
        @Override
        public User get(String id) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void deleteAll() { throw new UnsupportedOperationException(); }
        @Override
        public int getCount() { throw new UnsupportedOperationException(); }
    }
}
