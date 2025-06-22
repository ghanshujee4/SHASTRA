package com.library.sdl.payment;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailSender {

    public static void main(String[] args) {
        final String username = "info@manage.shastradigitallibrary.com";
        final String password = "lalanKUMAR1@";
        final String toEmail = "ghanshujee4@gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // Required for STARTTLS
        props.put("mail.smtp.host", "smtp.hostinger.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2"); // Ensure modern TLS


        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("info@manage.shastradigitallibrary.com", "lalanKUMAR1@");
            }
        });
        session.setDebug(true);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Test Email");
            message.setText("Hello, this is a test email from Java.");

            Transport.send(message);
            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

