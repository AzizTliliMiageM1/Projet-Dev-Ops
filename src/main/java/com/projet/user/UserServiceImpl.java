package com.projet.user;

import java.util.UUID;
import com.projet.email.EmailService;
import com.projet.email.EmailServiceImpl;

public class UserServiceImpl implements UserService {

    private final UserRepository repository = new FileUserRepository();

    @Override
    public String register(String email, String password) {

        // Vérifier si l'email existe déjà
        if (repository.findByEmail(email) != null) {
            return null; // email déjà utilisé
        }

        // Générer un token unique
        String token = UUID.randomUUID().toString();

        // Créer et sauvegarder l'utilisateur
        User user = new User(email, password, token);
        repository.save(user);

        // === 🔵 ENVOI EMAIL DE CONFIRMATION ===
        try {
            EmailService emailService = new EmailServiceImpl();

            String link = "http://localhost:4567/api/confirm?token=" + token;

            String message = 
                "Bonjour,\n\n" +
                "Merci pour votre inscription !\n" +
                "Veuillez confirmer votre compte en cliquant sur le lien ci-dessous :\n\n" +
                link + "\n\n" +
                "Cordialement,\nService DevOps";

            emailService.sendEmail(
                email,
                "Confirmez votre compte",
                message
            );

            System.out.println("Email de confirmation envoyé → " + email);

        } catch (Exception e) {
            System.err.println("⚠ Erreur lors de l’envoi de l’email : " + e.getMessage());
            e.printStackTrace();
        }

        // Retourner le token
        return token;
    }
}
