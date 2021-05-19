package service;

import dao.UserDao;
import domain.Level;
import domain.User;

import java.util.List;

public class UserService {

    UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void upgradeLevels() {
        List<User> users = userDao.getAll();
        for(User user: users) {
            Boolean changed = null; // 레벨의 변화가 있는지를 확인하는 플래그
            // basic 업그레이드 작업
            if (user.getLevel() == Level.BASIC && user.getLogin() >= 50) {
                user.setLevel(Level.SILVER);
                changed = true;
            }
            // silver 업그레이드 작업
            else if (user.getLevel() == Level.SILVER && user.getRecommend() >= 30) {
                user.setLevel(Level.GOLD);
                changed = true; // 레벨 변경 플래그 설정
            }
            else if (user.getLevel() == Level.GOLD) { changed = false; } // gold 레벨은 변경이 일어나지 않음
            else { changed = false; } // 일치하는 조건이 없으면 변경 없음

            // 레벨 변경이 있는 경우에만 update() 호출
            if (changed) userDao.update(user);
       }
    }
}
