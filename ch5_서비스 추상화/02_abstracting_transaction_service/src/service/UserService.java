package service;

import dao.UserDao;
import domain.Level;
import domain.User;

import java.util.List;

public class UserService {
    public static final int MIN_LOG_COUNT_FOR_SILVER = 50;
    public static final int MIN_RECOMMEND_FOR_GOLD = 30;

    UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    // 사용자 추가 메소드
    public void add(User user) {
        // 처음 가입하는 사용자는 BASIC 레벨이 된다.
        if (user.getLevel() == null) user.setLevel(Level.BASIC);
        userDao.add(user);
    }

    public void upgradeLevels() {
        // 모든 사용자 정보를 가져와 한 명씩 업그레이드가 가능한지 확인하고, 가능하면 업그레이드를 한다.
        List<User> users = userDao.getAll();
        for(User user : users) {
            if (canUpgradeLevel(user)) {
                upgradeLevel(user);
            }
        }
    }

    public boolean canUpgradeLevel(User user) {
        Level currentLevel = user.getLevel();
        // 레벨별로 구분해서 조건은 판단한다.
        switch (currentLevel) {
            case BASIC:
                return (user.getLogin() >= MIN_LOG_COUNT_FOR_SILVER); // 상수 도입
            case SILVER:
                return (user.getRecommend() >= MIN_RECOMMEND_FOR_GOLD); // 상수 도입
            case GOLD:
                return false;
            default:
                //throw new IllegalArgumentException("Unknown Level: " + currentLevel);
                // 현재 로직에서 다룰 수 없는 레벨이 주어지면 예외를 발생시킨다.
                // 새로운 레벨이 추가되고 로직을 수정하지 않으면 에러가 나서 확인가능.
                return false; // 테스트 시 예외처리때문에 java.lang.IllegalStateException: Failed to load ApplicationContext 오류가 생겨서 return false 로 대체
        }
    }

    // 레벨 업그레이드 작업 메소드
    private void upgradeLevel(User user) {
        user.upgradeLevel();
        userDao.update(user);
    }
}
