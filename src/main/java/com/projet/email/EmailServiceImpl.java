package com.projet.email;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailServiceImpl implements EmailService {

    private final Session session;
    private final String from;

    public EmailServiceImpl() {

        // Lire l’email et mdp depuis les variables d'environnement
        this.from = System.getenv().getOrDefault("SMTP_USER", "example@gmail.com");
        String password = System.getenv().getOrDefault("SMTP_PASS", "password");

        // Configuration SMTP (Gmail par défaut)
        Properties props = new Properties();
        props.put("mail.smtp.host", System.getenv().getOrDefault("SMTP_HOST", "smtp.gmail.com"));
        props.put("mail.smtp.port", System.getenv().getOrDefault("SMTP_PORT", "587"));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Authentification
        this.session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });
    }

    @Override
    public void sendEmail(String to, String subject, String message) {
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);
            msg.setText(message);

            Transport.send(msg);

            System.out.println("Email envoyé à : " + to);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }
}
