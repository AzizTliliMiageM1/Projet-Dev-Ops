package com.projet.api;

import java.util.Date;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Service d'envoi d'emails pour les notifications
 */
public class EmailService {
    
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static EmailService instance;
    
    private String fromEmail;
    private String password;
    private boolean enabled = false;
    
    private EmailService() {
        // Singleton
    }
    
    public static EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }
    
    /**
     * Configure le service email avec les identifiants SMTP
     */
    public void configure(String fromEmail, String password) {
        this.fromEmail = fromEmail;
        this.password = password;
        this.enabled = (fromEmail != null && !fromEmail.isEmpty() && 
                        password != null && !password.isEmpty());
    }
    
    /**
     * Envoie un email de test
     */
    public boolean sendTestEmail(String toEmail) {
        if (!enabled) {
            System.out.println("Service email non configuré");
            return false;
        }
        
        try {
            String subject = "✅ Test de notification - Gestion Abonnements";
            String body = buildTestEmailBody();
            
            sendEmail(toEmail, subject, body);
            System.out.println("Email de test envoyé à: " + toEmail);
            return true;
            
        } catch (Exception e) {
            System.err.println("Erreur envoi email de test: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Envoie une alerte d'expiration
     */
    public boolean sendExpirationAlert(String toEmail, String subscriptionName, 
                                      double price, String expirationDate, int daysRemaining) {
        if (!enabled) return false;
        
        try {
            String subject = String.format("⚠️ Alerte : %s expire dans %d jours", 
                                          subscriptionName, daysRemaining);
            String body = buildExpirationEmailBody(subscriptionName, price, 
                                                   expirationDate, daysRemaining);
            
            sendEmail(toEmail, subject, body);
            System.out.println("Alerte d'expiration envoyée pour: " + subscriptionName);
            return true;
            
        } catch (Exception e) {
            System.err.println("Erreur envoi alerte expiration: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Envoie une alerte de dépassement de budget
     */
    public boolean sendBudgetAlert(String toEmail, double budget, double spent, double overspend) {
        if (!enabled) return false;
        
        try {
            String subject = String.format("💸 Alerte Budget : Dépassement de %.2f€", overspend);
            String body = buildBudgetEmailBody(budget, spent, overspend);
            
            sendEmail(toEmail, subject, body);
            System.out.println("Alerte budget envoyée");
            return true;
            
        } catch (Exception e) {
            System.err.println("Erreur envoi alerte budget: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Envoie un rapport mensuel
     */
    public boolean sendMonthlyReport(String toEmail, String month, double totalSpent, 
                                    int totalSubs, double subsCost, int transactionCount) {
        if (!enabled) return false;
        
        try {
            String subject = String.format("📊 Résumé Mensuel - %s", month);
            String body = buildMonthlyReportBody(month, totalSpent, totalSubs, 
                                                subsCost, transactionCount);
            
            sendEmail(toEmail, subject, body);
            System.out.println("Rapport mensuel envoyé");
            return true;
            
        } catch (Exception e) {
            System.err.println("Erreur envoi rapport mensuel: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Méthode principale d'envoi d'email
     */
    private void sendEmail(String toEmail, String subject, String htmlBody) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setContent(htmlBody, "text/html; charset=utf-8");
        message.setSentDate(new Date());
        
        Transport.send(message);
    }
    
    // ========================================
    // TEMPLATES HTML
    // ========================================
    
    private String buildTestEmailBody() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Arial', sans-serif; background: #f4f4f4; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; }
                    .header { background: linear-gradient(135deg, #667eea, #764ba2); color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; }
                    .footer { background: #f9f9f9; padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    .button { background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Email de Test</h1>
                    </div>
                    <div class="content">
                        <h2>Félicitations !</h2>
                        <p>Votre configuration email fonctionne parfaitement.</p>
                        <p>Vous recevrez maintenant des notifications automatiques pour :</p>
                        <ul>
                            <li>Alertes d'expiration d'abonnements</li>
                            <li>Dépassements de budget</li>
                            <li>Rapports mensuels</li>
                            <li>Dépenses inhabituelles</li>
                        </ul>
                        <p>Date du test : %s</p>
                        <a href="http://localhost:4567/email-settings.html" class="button">Gérer mes notifications</a>
                    </div>
                    <div class="footer">
                        <p>Gestion Abonnements - Notifications automatiques</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(new Date().toString());
    }
    
    private String buildExpirationEmailBody(String subscriptionName, double price, 
                                            String expirationDate, int daysRemaining) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Arial', sans-serif; background: #f4f4f4; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; }
                    .header { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; }
                    .alert-box { background: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .details { background: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 0; }
                    .button { background: #f59e0b; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin-top: 20px; }
                    .footer { background: #f9f9f9; padding: 20px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ Alerte d'Expiration</h1>
                    </div>
                    <div class="content">
                        <h2>Bonjour,</h2>
                        <p>Votre abonnement <strong>%s</strong> arrive bientôt à expiration.</p>
                        
                        <div class="alert-box">
                            <strong>⏰ Expiration dans %d jours</strong>
                        </div>
                        
                        <div class="details">
                            <h3>Détails de l'abonnement :</h3>
                            <p><strong>Service :</strong> %s</p>
                            <p><strong>Prix :</strong> %.2f€/mois</p>
                            <p><strong>Date d'expiration :</strong> %s</p>
                        </div>
                        
                        <p>Pensez à renouveler votre abonnement pour continuer à profiter du service sans interruption.</p>
                        
                        <a href="http://localhost:4567/index.html" class="button">Gérer mes abonnements</a>
                    </div>
                    <div class="footer">
                        <p>Gestion Abonnements - Rappel automatique</p>
                    </div>
                </div>
            </body>
            </html>
            """, subscriptionName, daysRemaining, subscriptionName, price, expirationDate);
    }
    
    private String buildBudgetEmailBody(double budget, double spent, double overspend) {
        double percentage = (overspend / budget) * 100;
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Arial', sans-serif; background: #f4f4f4; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; }
                    .header { background: linear-gradient(135deg, #ef4444, #dc2626); color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; }
                    .alert-box { background: #fee2e2; border-left: 4px solid #ef4444; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .stats { display: flex; justify-content: space-around; margin: 20px 0; }
                    .stat { text-align: center; padding: 15px; background: #f9f9f9; border-radius: 5px; flex: 1; margin: 0 5px; }
                    .stat h3 { color: #ef4444; margin: 0; font-size: 24px; }
                    .button { background: #ef4444; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin-top: 20px; }
                    .footer { background: #f9f9f9; padding: 20px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>💸 Dépassement de Budget</h1>
                    </div>
                    <div class="content">
                        <h2>Attention !</h2>
                        <p>Vous avez dépassé votre budget mensuel.</p>
                        
                        <div class="alert-box">
                            <strong>⚠️ Dépassement de %.2f€ (%.1f%%)</strong>
                        </div>
                        
                        <div class="stats">
                            <div class="stat">
                                <h3>%.2f€</h3>
                                <p>Budget</p>
                            </div>
                            <div class="stat">
                                <h3>%.2f€</h3>
                                <p>Dépensé</p>
                            </div>
                            <div class="stat">
                                <h3>%.2f€</h3>
                                <p>Dépassement</p>
                            </div>
                        </div>
                        
                        <p>Consultez vos dépenses pour identifier les postes à optimiser.</p>
                        
                        <a href="http://localhost:4567/expenses.html" class="button">Voir mes dépenses</a>
                    </div>
                    <div class="footer">
                        <p>Gestion Abonnements - Alerte budget</p>
                    </div>
                </div>
            </body>
            </html>
            """, overspend, percentage, budget, spent, overspend);
    }
    
    private String buildMonthlyReportBody(String month, double totalSpent, int totalSubs, 
                                         double subsCost, int transactionCount) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Arial', sans-serif; background: #f4f4f4; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; }
                    .header { background: linear-gradient(135deg, #667eea, #764ba2); color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; }
                    .kpi-grid { display: flex; flex-wrap: wrap; gap: 15px; margin: 20px 0; }
                    .kpi { flex: 1; min-width: 120px; text-align: center; padding: 20px; background: linear-gradient(135deg, #667eea, #764ba2); color: white; border-radius: 10px; }
                    .kpi h3 { margin: 0; font-size: 28px; }
                    .kpi p { margin: 5px 0 0 0; opacity: 0.9; }
                    .summary { background: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 0; }
                    .button { background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin-top: 20px; }
                    .footer { background: #f9f9f9; padding: 20px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📊 Résumé Mensuel</h1>
                        <p style="opacity: 0.9;">%s</p>
                    </div>
                    <div class="content">
                        <h2>Bonjour,</h2>
                        <p>Voici votre bilan financier du mois.</p>
                        
                        <div class="kpi-grid">
                            <div class="kpi">
                                <h3>%.2f€</h3>
                                <p>Total dépensé</p>
                            </div>
                            <div class="kpi">
                                <h3>%d</h3>
                                <p>Abonnements</p>
                            </div>
                            <div class="kpi">
                                <h3>%.2f€</h3>
                                <p>Coût abonnements</p>
                            </div>
                            <div class="kpi">
                                <h3>%d</h3>
                                <p>Transactions</p>
                            </div>
                        </div>
                        
                        <div class="summary">
                            <h3>Points clés :</h3>
                            <ul>
                                <li>Total des dépenses : %.2f€</li>
                                <li>Abonnements actifs : %d</li>
                                <li>Nombre de transactions : %d</li>
                                <li>Coût mensuel des abonnements : %.2f€</li>
                            </ul>
                        </div>
                        
                        <p>Consultez votre tableau de bord pour plus de détails.</p>
                        
                        <a href="http://localhost:4567/index.html" class="button">Voir le rapport complet</a>
                    </div>
                    <div class="footer">
                        <p>Gestion Abonnements - Rapport mensuel automatique</p>
                    </div>
                </div>
            </body>
            </html>
            """, month, totalSpent, totalSubs, subsCost, transactionCount, 
                 totalSpent, totalSubs, transactionCount, subsCost);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
}
