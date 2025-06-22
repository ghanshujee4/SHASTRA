package com.library.sdl.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentRecordController {

    @Autowired
    private PaymentRecordService paymentRecordService;

    @PostMapping("/{userId}/pay")
    public ResponseEntity<?> makePayment(@PathVariable Long userId, @RequestBody PaymentRecord paymentRequest) {
        paymentRecordService.createMonthlyPayment(userId,paymentRequest.getAmount(),paymentRequest.getComments()); // Monthly Fee: 500
        return ResponseEntity.ok("Payment Recorded");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<PaymentRecord>> getPayments(@PathVariable Long userId) {
        List<PaymentRecord> payments = paymentRecordService.getUserPayments(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(payments);
    }

    @PostMapping("/mark-as-paid/{paymentId}")
    public ResponseEntity<?> markAsPaid(@PathVariable Long paymentId, @RequestBody PaymentRecord paymentRequest) {
        paymentRecordService.markAsPaid(paymentId, paymentRequest.getAmount(), paymentRequest.getComments());
        return ResponseEntity.ok().build();
    }

    // Update a payment record
    @PutMapping("/{paymentId}")
    public ResponseEntity<?> updatePayment(@PathVariable Long paymentId, @RequestBody PaymentRecord updatedPayment) {
        paymentRecordService.updatePayment(paymentId, updatedPayment);
        return ResponseEntity.ok("Payment Updated Successfully");
    }

    // Delete a payment record
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<?> deletePayment(@PathVariable Long paymentId) {
        paymentRecordService.deletePayment(paymentId);
        return ResponseEntity.ok("Payment Deleted Successfully");
    }

    @Autowired
    private EmailService emailService;

    // Method to send reminders to overdue payments
    @PostMapping("/send-overdue-reminders/{userId}")
    public ResponseEntity<String> sendOverdueReminder(@PathVariable Long userId) {
        List<PaymentRecord> overduePayments = paymentRecordService.getOverduePayments(userId);

        if (overduePayments.isEmpty()) {
            return ResponseEntity.ok("No overdue payments found.");
        }

        // Assuming we have a method in the user service to get the user's email
        String userEmail = "ghanshujee4@gmail.com";  // Replace this with actual "user fetching logic"
        String subject = "Payment Reminder: Your payment is overdue";
        StringBuilder body = new StringBuilder();
        body.append("<h3>Dear User,</h3>")
                .append("<p>Your payment is overdue. Please make the payment as soon as possible.</p>")
                .append("<ul>");

        // Append each overdue payment to the email body
        for (PaymentRecord payment : overduePayments) {
            body.append("<li>")
                    .append("Amount: ").append(payment.getAmount())
                    .append(", Due Date: ").append(payment.getDueDate())
                    .append("</li>");
        }
        body.append("</ul>")
                .append("<p>Please make the payment at your earliest convenience.</p>");

        // Send the email
        try {
            emailService.sendPaymentReminder(userEmail, subject, body.toString());
            return ResponseEntity.ok("Reminder email sent successfully.");
        } catch (MessagingException | jakarta.mail.MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send reminder email: " + e.getMessage());
        }
    }
    @GetMapping("/overdue")
    public ResponseEntity<List<PaymentRecord>> getOverduePayments() {
        List<PaymentRecord> overduePayments = paymentRecordService.getAllOverduePayments();

        // No overdue payments

        return ResponseEntity.ok().body(overduePayments);
    }
}

