package com.projet.email;

public interface EmailService {

    /**
     * Envoie un email simple en texte brut.
     *
     * @param to destinataire
     * @param subject sujet du mail
     * @param message contenu du mail
     */
    void sendEmail(String to, String subject, String message);
}
