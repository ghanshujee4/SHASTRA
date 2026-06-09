package com.library.sdl.auth;

import com.library.sdl.User;
import com.library.sdl.UserRepository;
import com.library.sdl.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    @Value("${app.reset.base-url:https://manage.shastradigitallibrary.com}")
    private String baseUrl;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        // Invalidate previous tokens for this user
        tokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);
        PasswordResetToken prt = new PasswordResetToken(token, user, expiry);
        tokenRepository.save(prt);

        // Build reset URL
        String resetUrl = baseUrl + "/reset-password?token=" + token;

        String subject = "SDL Password Reset Request";
        String body = "Dear " + user.getName() + ",\n\n"
                + "We received a request to reset your password. Click the link below to reset it (valid for 30 minutes):\n\n"
                + resetUrl + "\n\n"
                + "token : "  +token + "\n\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "Thank you,\nTeam SDL";

        // Send email (non-blocking in EmailService)
        try {
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            logger.warn("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
        }

        return token;
    }

    public Optional<User> validateToken(String token) {
        Optional<PasswordResetToken> opt = tokenRepository.findByToken(token);
        if (opt.isEmpty()) return Optional.empty();
        PasswordResetToken prt = opt.get();
        if (!prt.isValid()) return Optional.empty();
        return Optional.of(prt.getUser());
    }

    @Transactional
    public User resetPassword(String token, String newPassword) {
        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (!prt.isValid()) {
            throw new RuntimeException("Token expired or already used");
        }

        User user = prt.getUser();
        user.setPassword(newPassword);
        userRepository.save(user);

        prt.setUsed(true);
        tokenRepository.save(prt);

        return user;
    }
}

