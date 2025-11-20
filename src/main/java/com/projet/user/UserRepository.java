package com.projet.user;

public interface UserRepository {
    void save(User user);
    User findByEmail(String email);
    User findByToken(String token);
    void update(User user);
}
