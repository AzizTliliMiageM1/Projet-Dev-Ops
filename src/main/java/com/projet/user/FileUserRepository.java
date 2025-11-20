package com.projet.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FileUserRepository implements UserRepository {

    private final File file = new File("users-db.txt");

    @Override
    public void save(User user) {
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(user.getEmail() + ";" + user.getPassword() + ";" + user.isConfirmed() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public User findByEmail(String email) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(";");
                if (p[0].equals(email)) {
                    User u = new User(p[0], p[1]);
                    u.setConfirmed(Boolean.parseBoolean(p[2]));
                    return u;
                }
            }
        } catch (Exception e) {}
        return null;
    }
}
