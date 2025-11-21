package com.projet.email;

import org.eclipse.angus.mail.smtp.SMTPTransport;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailServiceImpl implements EmailService {

    private final Session session;
    private final String from;
    private final String password;

    public EmailServiceImpl() {

        this.from = "f.mayssara@gmail.com";  
        this.password = "cxkvwfquhrjykteo"; // mot de passe d'application Gmail

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });
    }

    @Override
    public void sendEmail(String to, String subject, String htmlMessage) {
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Sujet avec encodage UTF-8
            msg.setSubject(subject, "UTF-8");

            // Très important : envoi en HTML
            msg.setContent(htmlMessage, "text/html; charset=UTF-8");

            // Envoi SMTP
            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
            t.connect();
            t.sendMessage(msg, msg.getAllRecipients());
            t.close();

            System.out.println("Email HTML envoyé → " + to);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur email : " + e.getMessage());
        }
    }
}
