package com.library.sdl.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/sendToUser")
    public String sendToUser(@RequestParam String email) {
        emailService.sendEmailToUser(
                email,
                "Welcome to SDL 🎉",
                "Dear Student,\n\nThank you for registering at Shastra Digital Library!"
        );
        return "Email sent (if user found)";
    }

    @PostMapping("/sendBulk")
    public String sendBulkEmails(@RequestBody EmailRequest request) {
        request.getEmails().forEach(email ->
                emailService.sendEmailToUser(email, request.getSubject(), request.getBody())
        );
        return "Bulk emails sent successfully";
    }


}
