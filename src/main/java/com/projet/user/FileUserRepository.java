package com.projet.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class FileUserRepository implements UserRepository {

    private final File file = new File("users-db.txt");

    @Override
    public void save(User user) {
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(user.getEmail() + ";" 
                    + user.getPassword() + ";" 
                    + user.isConfirmed() + ";" 
                    + user.getConfirmationToken() + ";"
                    + user.getPseudo() + "\n");
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
                    String pseudo = p.length > 4 ? p[4] : email.split("@")[0];
                    User u = new User(p[0], p[1], pseudo, p[3]);
                    u.setConfirmed(Boolean.parseBoolean(p[2]));
                    return u;
                }
            }
        } catch (Exception e) {}
        return null;
    }

    @Override
    public User findByToken(String token) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(";");
                if (p.length > 3 && p[3].equals(token)) {
                    String pseudo = p.length > 4 ? p[4] : p[0].split("@")[0];
                    User u = new User(p[0], p[1], pseudo, p[3]);
                    u.setConfirmed(Boolean.parseBoolean(p[2]));
                    return u;
                }
            }
        } catch (Exception e) {}
        return null;
    }

    @Override
    public void update(User user) {
        try {
            List<String> lines = new ArrayList<>();

            if (!file.exists()) return;

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(";");
                    if (p[0].equals(user.getEmail())) {
                        line = user.getEmail() + ";" 
                             + user.getPassword() + ";" 
                             + user.isConfirmed() + ";" 
                             + user.getConfirmationToken() + ";"
                             + user.getPseudo();
                    }
                    lines.add(line);
                }
            }

            try (FileWriter fw = new FileWriter(file)) {
                for (String l : lines) fw.write(l + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
