package com.projet.user;

import java.util.UUID;

public class UserServiceImpl implements UserService {

    private final UserRepository repository = new FileUserRepository();

    @Override
    public String register(String email, String password) {

        if (repository.findByEmail(email) != null) {
            return null; // email déjà utilisé
        }

        String token = UUID.randomUUID().toString();

        User user = new User(email, password, token);
        repository.save(user);

        return token;
    }
}
