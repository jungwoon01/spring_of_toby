import dao.UserDao;
import domain.Level;
import domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserDaoTest {
    private User user1;
    private User user2;
    private User user3;

    //@Autowired 는 스프링의 컨텍스트 내에서 정의된 빈 중에서 인스턴스 변수 주입 가능한 타입의 빈을 찾아준다.
    // UserDao 는 UserDAoJdbc 가 구현한 인터페이스이므로
    // UserDaoTest 의 dao 변수에 UserDaoJdbc 클래스로 정의된 빈을 넣는 데 아무런 문제가 없다.
    @Autowired
    UserDao dao;
    // DataSource 빈을 주입받도록 만든 UserDaoTest
    @Autowired
    DataSource dataSource;

    @Before
    public void setUp() {
        this.user1 = new User("bbb", "철수", "1234", Level.BASIC, 1, 0);
        this.user2 = new User("ccc", "짱구", "4321", Level.SILVER, 55, 10);
        this.user3 = new User("aaa", "유리", "5678", Level.GOLD, 100, 40);
    }


    @Test
    public void addAndGet() throws SQLException, ClassNotFoundException {

        dao.deleteAll();
        assertThat(dao.getCount(), is(0));
        // deleteAll() 후에 getCount()리턴 값이 0이면 deleteAll() 성공
        // 하지만 getCount() 디폴트가 0 일수도 있기 때문에 아래에서 한번 더 검증

        dao.add(user1);
        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        User userGet1 = dao.get(user1.getId());
        checkSameUser(userGet1, user1);

        User userGet2 = dao.get(user2.getId());
        checkSameUser(userGet2, user2);

    }


    @Test
    public void count() throws SQLException, ClassNotFoundException {
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        assertThat(dao.getCount(), is(1));

        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        dao.add(user3);
        assertThat(dao.getCount(), is(3));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void getUserFailure() throws SQLException, ClassNotFoundException {

        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.get("unknown_id");
    }

    @Test
    public void getAll() throws SQLException {
        dao.deleteAll(); // 검사의 정확성을 위해 모든 데이터를 지운다.

        List<User> users0 = dao.getAll();
        assertThat(users0.size(), is(0)); // 데이터가 없을 때는 크기가 0인 리스트 오브젝트가 리턴돼야 한다.

        dao.add(user1); // Id : bbb
        List<User> users1 = dao.getAll();
        assertThat(users1.size(), is(1));
        checkSameUser(user1, users1.get(0));

        dao.add(user2); // Id : ccc
        List<User> users2 = dao.getAll();
        assertThat(users2.size(), is(2));
        checkSameUser(user1, users2.get(0));
        checkSameUser(user2, users2.get(1));

        dao.add(user3); // Id : aaa
        List<User> users3 = dao.getAll();
        assertThat(users3.size(), is(3));
        checkSameUser(user3, users3.get(0)); // user3 의 id 값이 가장 빠르므로 첫 번째 엘리먼트여야 한다.
        checkSameUser(user1, users3.get(1));
        checkSameUser(user2, users3.get(2));
    }

    // 예외가 발생하면 성성이고 아니면 실패하게 만들어야하므로 예외를 검증해주는 @Test(expected=...)를 이용한다.
    // 메소드가 끝날 때까지 예외가 발생하지 않으면 테스트는 실패하게 된다.
    @Test(expected = DataAccessException.class) // DuplicatedKeyException 은 DataAccessException 의 서브 클래스이므로 DuplicateKeyException 으로 바꾸고 실행해도 테스트는 성공한다.
    public void duplicateKey() {
        dao.deleteAll();

        dao.add(user1);
        dao.add(user1); // 강제로 같은 사용자를 두 번 등록한다. 여기서 예외가 발생해야 한다.
    }

    @Test
    public void sqlExceptionTranslate() {
        dao.deleteAll();

        try {
            dao.add(user1);
            dao.add(user1); // 강제로 DuplicateKeyException 을 발생
        } catch (DuplicateKeyException exception) {
            SQLException sqlEx = (SQLException) exception.getRootCause(); // 중첩되어 있는 SQLException 을 가져올 수 있다.

            // 주입 받은 dataSource 를 이용해 SQLErrorCodeSQLExceptionTranslator 의 오브젝트를 만든다.
            // 그리고 SQLException 을 파라미터로 넣어서 translate() 메소드를 호출해주면
            // SQLException 을 DataAccessException 타입의 예외로 변환해준다.
            SQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(this.dataSource);
            // assertThat()의 is() 메소드에 클래스를 넣으면 오브젝트의 equals() 비교 대신 주어진 클래스의 인스턴스인지 검사해준다.
            assertThat(set.translate(null, null, sqlEx), is(DuplicateKeyException.class));
        }
    }

    // 어노테이션이 붙지 않은 메소드에 테스트 코드에서 반복적으로 나타나는 코드를 담아두고 재사용하는 건 좋은 습관이다.
    // 여러 테스트 클래스에 걸쳐 재사용되는 코드라면 별도의 클래스로 불리하는 것도 고려해볼 수 있다.
    private void checkSameUser(User user1, User user2) {
        assertThat(user1.getId(), is(user2.getId()));
        assertThat(user1.getName(), is(user2.getName()));
        assertThat(user1.getPassword(), is(user2.getPassword()));
        assertThat(user1.getLogin(), is(user2.getLogin()));
        assertThat(user1.getLogin(), is(user2.getLogin()));
        assertThat(user1.getRecommend(), is(user2.getRecommend()));
    }
}
