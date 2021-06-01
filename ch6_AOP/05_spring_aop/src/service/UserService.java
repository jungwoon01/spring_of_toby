package service;

import domain.User;

public interface UserService {
    void add(User user);
    void upgradeLevels();
}
