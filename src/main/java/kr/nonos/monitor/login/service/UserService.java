package kr.nonos.monitor.login.service;

import kr.nonos.monitor.login.entity.User;

public interface UserService {
    public User findUserByEmail(String email);
    public User findUserByUserId(int id);
    public void saveUser(User user);
    public void deleteUser(User user);
}