import dao.UserDao;
import domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserDaoTest {
    private User user1;
    private User user2;
    private User user3;

    @Autowired
    UserDao dao;

    @Before
    public void setUp() {
        this.user1 = new User("bbb", "철수", "1234");
        this.user2 = new User("ccc", "짱구", "4321");
        this.user3 = new User("aaa", "유리", "5678");
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
        assertThat(userGet1.getName(), is(user1.getName()));
        assertThat(userGet1.getPassword(), is(user1.getPassword()));

        User userGet2 = dao.get(user2.getId());
        assertThat(userGet2.getName(), is(user2.getName()));
        assertThat(userGet2.getPassword(), is(user2.getPassword()));

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

    // 어노테이션이 붙지 않은 메소드에 테스트 코드에서 반복적으로 나타나는 코드를 담아두고 재사용하는 건 좋은 습관이다.
    // 여러 테스트 클래스에 걸쳐 재사용되는 코드라면 별도의 클래스로 불리하는 것도 고려해볼 수 있다.
    private void checkSameUser(User user1, User user2) {
        assertThat(user1.getId(), is(user2.getId()));
        assertThat(user1.getName(), is(user2.getName()));
        assertThat(user1.getPassword(), is(user2.getPassword()));
    }
}
