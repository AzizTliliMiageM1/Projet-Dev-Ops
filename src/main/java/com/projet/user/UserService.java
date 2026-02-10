package com.projet.user;

import com.projet.backend.domain.User;

public interface UserService {
    String register(String email, String password, String pseudo);
}
