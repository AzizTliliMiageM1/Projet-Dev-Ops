package com.projet.user;

import java.util.UUID;

import com.projet.email.EmailServiceImpl;

public class UserServiceImpl implements UserService {

    private final UserRepository repository = new FileUserRepository();

    @Override
    public String register(String email, String password, String pseudo) {

        // Vérifie si l'email existe déjà
        if (repository.findByEmail(email) != null) {
            return null;
        }

        // Génération du token
        String token = UUID.randomUUID().toString();

        // Création utilisateur
        User user = new User(email, password, pseudo, token);
        repository.save(user);

        // -------------------------------
        // URL du bouton de confirmation
        // -------------------------------
        String baseUrl = System.getenv().getOrDefault("APP_BASE_URL", "http://localhost:4567");
        String link = baseUrl + "/api/confirm?token=" + token;

        // -------------------------------
        // Email HTML avec bouton - Design GestionPro
        // -------------------------------
        String htmlMessage = """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Confirmation d'inscription - GestionPro</title>
        </head>
        <body style="margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);">
            <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                <tr>
                    <td style="padding: 40px 20px;">
                        <table role="presentation" style="max-width: 600px; margin: 0 auto; background: rgba(255, 255, 255, 0.98); border-radius: 24px; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25); overflow: hidden;">
                            
                            <!-- Header avec logo -->
                            <tr>
                                <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                                    <div style="display: inline-block; background: rgba(255, 255, 255, 0.15); padding: 16px; border-radius: 16px; backdrop-filter: blur(10px); margin-bottom: 20px;">
                                        <div style="width: 60px; height: 60px; background: linear-gradient(135deg, #f59e0b, #ea580c); border-radius: 50%%; display: inline-flex; align-items: center; justify-content: center; font-size: 32px; color: white;">
                                            📊
                                        </div>
                                    </div>
                                    <h1 style="margin: 0; color: white; font-size: 32px; font-weight: 800;">GestionPro</h1>
                                    <p style="margin: 8px 0 0 0; color: rgba(255, 255, 255, 0.9); font-size: 14px;">Gestion d'abonnements moderne</p>
                                </td>
                            </tr>
                            
                            <!-- Contenu principal -->
                            <tr>
                                <td style="padding: 50px 40px;">
                                    <h2 style="margin: 0 0 24px 0; color: #1f2937; font-size: 28px; font-weight: 700; text-align: center;">
                                        Confirmez votre inscription
                                    </h2>
                                    
                                    <p style="margin: 0 0 16px 0; color: #4b5563; font-size: 16px; line-height: 1.6;">
                                        Bonjour,
                                    </p>
                                    
                                    <p style="margin: 0 0 32px 0; color: #4b5563; font-size: 16px; line-height: 1.6;">
                                        Merci pour votre inscription sur <strong style="color: #667eea;">GestionPro</strong>. Pour activer votre compte et commencer à optimiser vos abonnements, cliquez sur le bouton ci-dessous :
                                    </p>
                                    
                                    <!-- Bouton CTA -->
                                    <table role="presentation" style="width: 100%%; margin: 40px 0;">
                                        <tr>
                                            <td style="text-align: center;">
                                                <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #f59e0b, #ea580c); color: white; padding: 18px 48px; text-decoration: none; border-radius: 16px; font-size: 18px; font-weight: 700; box-shadow: 0 10px 15px -3px rgba(245, 158, 11, 0.4); transition: all 0.3s ease;">
                                                    ✓ Confirmer mon compte
                                                </a>
                                            </td>
                                        </tr>
                                    </table>
                                    
                                    <!-- Lien alternatif -->
                                    <div style="background: #f3f4f6; border-left: 4px solid #667eea; padding: 20px; border-radius: 8px; margin: 32px 0;">
                                        <p style="margin: 0 0 8px 0; color: #6b7280; font-size: 14px; font-weight: 600;">
                                            Ou copiez-collez ce lien dans votre navigateur :
                                        </p>
                                        <p style="margin: 0; color: #667eea; font-size: 13px; word-break: break-all; font-family: monospace;">
                                            %s
                                        </p>
                                    </div>
                                    
                                    <!-- Avantages -->
                                    <div style="margin: 40px 0 0 0; padding: 24px; background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05)); border-radius: 12px;">
                                        <p style="margin: 0 0 16px 0; color: #1f2937; font-size: 16px; font-weight: 600;">
                                            🚀 Ce qui vous attend :
                                        </p>
                                        <ul style="margin: 0; padding: 0 0 0 20px; color: #4b5563; font-size: 14px; line-height: 1.8;">
                                            <li>Interface moderne et intuitive</li>
                                            <li>Analytics avancées en temps réel</li>
                                            <li>Alertes intelligentes d'inactivité</li>
                                            <li>Export/Import JSON</li>
                                            <li>API REST complète</li>
                                        </ul>
                                    </div>
                                </td>
                            </tr>
                            
                            <!-- Footer -->
                            <tr>
                                <td style="background: #f9fafb; padding: 30px 40px; border-top: 1px solid #e5e7eb;">
                                    <p style="margin: 0 0 16px 0; color: #6b7280; font-size: 13px; line-height: 1.6; text-align: center;">
                                        Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.
                                    </p>
                                    
                                    <div style="text-align: center; margin-top: 24px;">
                                        <p style="margin: 0; color: #9ca3af; font-size: 12px;">
                                            © 2024 GestionPro - Gestion d'abonnements moderne
                                        </p>
                                        <p style="margin: 8px 0 0 0; color: #9ca3af; font-size: 12px;">
                                            <a href="http://localhost:4567/help.html" style="color: #667eea; text-decoration: none; margin: 0 8px;">Aide</a> •
                                            <a href="http://localhost:4567/api.html" style="color: #667eea; text-decoration: none; margin: 0 8px;">API</a> •
                                            <a href="http://localhost:4567" style="color: #667eea; text-decoration: none; margin: 0 8px;">Dashboard</a>
                                        </p>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(link, link);

        // Envoi du mail HTML
        EmailServiceImpl mail = new EmailServiceImpl();
        mail.sendEmail(email, "Confirmation d'inscription", htmlMessage);

        return token;
    }
}
