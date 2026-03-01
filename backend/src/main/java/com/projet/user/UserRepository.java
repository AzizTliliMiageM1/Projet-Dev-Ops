package com.projet.user;

import com.projet.backend.domain.User;

public interface UserRepository {
    void save(User user);
    User findByEmail(String email);
    User findByToken(String token);
    void update(User user);
}
