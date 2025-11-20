package com.projet.user;

public class UserServiceImpl implements UserService {

    private final UserRepository repository = new FileUserRepository();

    @Override
    public boolean register(String email, String password) {
        if (repository.findByEmail(email) != null) {
            return false; // email déjà utilisé
        }
        User u = new User(email, password);
        repository.save(u);
        return true;
    }
}
