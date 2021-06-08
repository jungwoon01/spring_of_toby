package test;

import domain.Level;
import domain.User;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UserTest {

    User user;

    @Before
    public void setUp() {
        user = new User();
    }

    // Level 이늄에 정의된 모든 레벨을 가져와서 User 에 설정해 두고
    // User 의 upgradeLevel() 을 실행해서 다음 레벨로 바뀌는지를 확인하는 메서드
    // 단, 다음 단계가 null 인것은 제외
    @Test
    public void upgradeLevel() {
        Level[] levels = Level.values();
        for(Level level : levels) {
            if (level.nextLevel() == null) continue;
            user.setLevel(level);
            user.upgradeLevel();
            assertThat(user.getLevel(), is(level.nextLevel()));
        }
    }

    // 더 이상 업그레이드할 레벨이 없는 경우에 upgradeLeve() 을 호출하면 예외가 발생하는지를 확인하는 테스트.
    @Test(expected = IllegalArgumentException.class)
    public void cannotUpgradeLevel() {
        Level[] levels = Level.values();
        for (Level level : levels) {
            if (level.nextLevel() != null) continue;
            user.setLevel(level);
            user.upgradeLevel();
        }
    }
}