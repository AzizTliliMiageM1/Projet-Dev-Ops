package com.projet.user;

import java.util.UUID;
import com.projet.email.EmailServiceImpl;

public class UserServiceImpl implements UserService {

    private final UserRepository repository = new FileUserRepository();

    @Override
    public String register(String email, String password) {

        // Vérifie si l'email existe déjà
        if (repository.findByEmail(email) != null) {
            return null;
        }

        // Génération du token
        String token = UUID.randomUUID().toString();

        // Création utilisateur
        User user = new User(email, password, token);
        repository.save(user);

        // -------------------------------
        // URL du bouton de confirmation
        // -------------------------------
        String baseUrl = System.getenv().getOrDefault("APP_BASE_URL", "http://localhost:4567");
        String link = baseUrl + "/api/confirm?token=" + token;

        // -------------------------------
        // Email HTML avec bouton
        // -------------------------------
        String htmlMessage = """
        <html>
        <body style="font-family: Arial, sans-serif; background: #f4f4f4; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background: white; padding: 20px; border-radius: 8px;">
                <h2 style="color: #333;">Confirmez votre inscription</h2>

                <p>Bonjour,</p>
                <p>Merci pour votre inscription. Pour activer votre compte, cliquez sur le bouton ci-dessous :</p>

                <div style="text-align: center; margin: 40px 0;">
                    <a href="%s"
                       style="background: #4CAF50; color: white; padding: 14px 28px;
                              text-decoration: none; border-radius: 6px; font-size: 16px;">
                        Confirmer mon compte
                    </a>
                </div>

                <p>Ou copiez-collez ce lien dans votre navigateur :</p>
                <p>%s</p>

                <p style="margin-top:40px; font-size:12px; color:#777;">
                    Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.
                </p>
            </div>
        </body>
        </html>
        """.formatted(link, link);

        // Envoi du mail HTML
        EmailServiceImpl mail = new EmailServiceImpl();
        mail.sendEmail(email, "Confirmation d'inscription", htmlMessage);

        return token;
    }
}
