package Fintech.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Service d'envoi d'email via Gmail SMTP.
 * Utilise l'authentification par mot de passe d'application Gmail.
 */
public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SENDER_EMAIL = "azizsoltani3578@gmail.com";
    // Mot de passe d'application Gmail (espaces supprimés)
    private static final String SENDER_APP_PW = "fjfbkaytvurpltbr";

    /**
     * Envoie un email texte simple.
     *
     * @param to      Adresse email du destinataire
     * @param subject Sujet du message
     * @param body    Corps du message
     * @throws MessagingException en cas d'erreur SMTP
     */
    public static void sendEmail(String to, String subject, String body) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PW);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}
