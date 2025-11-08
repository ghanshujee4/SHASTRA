package com.library.sdl.email;

import com.library.sdl.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String senderEmailUsername;

    public void sendEmail(String to, String subject, String body) {
        if (to == null || to.isEmpty()) {
            System.out.println("❌ Email not provided");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmailUsername);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            System.out.println("✅ Email sent successfully to " + to);

        } catch (Exception e) {
            System.err.println("⚠️ Email sending failed for " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendEmailToUser(String email, String subject, String body) {
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            sendEmail(user.getEmail(), subject, body);
        }, () -> {
            System.out.println("❌ No user found with email " + email);
        });
    }
}
