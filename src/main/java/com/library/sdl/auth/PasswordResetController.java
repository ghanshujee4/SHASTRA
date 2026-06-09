package com.library.sdl.auth;

import com.library.sdl.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        try {
            String token = passwordResetService.createPasswordResetToken(req.getEmail());
            logger.info("Password reset requested for {}", req.getEmail());
            return ResponseEntity.ok("Password reset email sent if the account exists.");
        } catch (Exception e) {
            logger.warn("Forgot password failed for {}: {}", req.getEmail(), e.getMessage());
            // Do not reveal whether email exists
            return ResponseEntity.ok("Password reset email sent if the account exists.");
        }
    }

    @GetMapping("/verify-token")
    public ResponseEntity<String> verifyToken(@RequestParam String token) {
        return passwordResetService.validateToken(token)
                .map(user -> ResponseEntity.ok("Token valid"))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            User user = passwordResetService.resetPassword(req.getToken(), req.getPassword());
            logger.info("Password reset successful for user {}", user.getEmail());
            return ResponseEntity.ok("Password reset successful");
        } catch (Exception e) {
            logger.warn("Password reset failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

