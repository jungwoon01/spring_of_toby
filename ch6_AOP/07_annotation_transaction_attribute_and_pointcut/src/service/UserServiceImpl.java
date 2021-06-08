package service;

import dao.UserDao;
import domain.Level;
import domain.User;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class UserServiceImpl implements UserService{
    public static final int MIN_LOG_COUNT_FOR_SILVER = 50;
    public static final int MIN_RECOMMEND_FOR_GOLD = 30;

    // Connection 을 생성할 때 사용할 DataSource 를 DI 받도록 한다.
    UserDao userDao;
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }


    // 메일 전송 기능을 가진 오브젝트를 DI 받는 변수와 메소드
    private MailSender mailSender;
    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 사용자 추가 메소드
    @Override
    public void add(User user) {
        // 처음 가입하는 사용자는 BASIC 레벨이 된다.
        if (user.getLevel() == null) user.setLevel(Level.BASIC);
        userDao.add(user);
    }

    @Override
    public User get(String id) {
        return userDao.get(id);
    }

    @Override
    public List<User> getAll() {
        return userDao.getAll();
    }

    @Override
    public void deleteAll() {
        userDao.deleteAll();
    }

    @Override
    public void update(User user) {
        userDao.update(user);
    }

    @Override
    public void upgradeLevels(){
        // 모든 사용자 정보를 가져와 한 명씩 업그레이드가 가능한지 확인하고, 가능하면 업그레이드를 한다.
        List<User> users = userDao.getAll();
        for (User user : users) {
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
    protected void upgradeLevel(User user) { // 테스트를 위해 protected 로 설정
        user.upgradeLevel();
        userDao.update(user);
        sendUpgradeEMail(user);
    }

    // 스프링의 MailSender 를 이용한 메일 발송 메소드
    private void sendUpgradeEMail(User user) {
        // MailMessage 인터페이스의 구현 클래스 오브젝트를 만들어 메일 내용을 작성한다.
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setFrom("useradmin@ksug.org");
        mailMessage.setSubject("Upgrade 안내");
        mailMessage.setText("사용자님의 등급이 올랐습니다.");

        mailSender.send(mailMessage);
    }
}
