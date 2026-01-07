package com.library.sdl.payment;

import com.library.sdl.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentRecordController {

    @Autowired
    private PaymentRecordService paymentRecordService;

    // ✅ NEW: Create new payment record (manual add)
    @PostMapping("/{userId}/add")
    public ResponseEntity<?> addPaymentRow(@PathVariable Long userId, @RequestBody PaymentRecord paymentRecord) {
        try {
            PaymentRecord newPayment = paymentRecordService.addPaymentRecord(userId, paymentRecord);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("New payment record created successfully with ID: " + newPayment.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create new payment record: " + e.getMessage());
        }
    }

    @PostMapping("/{userId}/pay")
    public ResponseEntity<String> makePayment(
            @PathVariable Long userId,
            @RequestBody(required = false) PaymentRecord paymentRequest
    ) {
        try {
            String comments = (paymentRequest != null && paymentRequest.getComments() != null)
                    ? paymentRequest.getComments()
                    : "Monthly Fee";

            paymentRecordService.createMonthlyPayment(userId, comments);
            return ResponseEntity.ok("Payment recorded successfully for user ID: " + userId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to record payment: " + e.getMessage());
        }
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

    @PutMapping("/{paymentId}")
    public ResponseEntity<?> updatePayment(@PathVariable Long paymentId, @RequestBody PaymentRecord updatedPayment) {
        paymentRecordService.updatePayment(paymentId, updatedPayment);
        return ResponseEntity.ok("Payment Updated Successfully");
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<?> deletePayment(@PathVariable Long paymentId) {
        paymentRecordService.deletePayment(paymentId);
        return ResponseEntity.ok("Payment Deleted Successfully");
    }

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-overdue-reminders/{userId}")
    public ResponseEntity<String> sendOverdueReminder(@PathVariable Long userId) {
        List<PaymentRecord> overduePayments = paymentRecordService.getOverduePayments(userId);

        if (overduePayments.isEmpty()) {
            return ResponseEntity.ok("No overdue payments found.");
        }

        String userEmail = "ghanshujee4@gmail.com";
        String subject = "Payment Reminder: Your payment is overdue";
        StringBuilder body = new StringBuilder();
        body.append("<h3>Dear User,</h3>")
                .append("<p>Your payment is overdue. Please make the payment as soon as possible.</p>")
                .append("<ul>");

        for (PaymentRecord payment : overduePayments) {
            body.append("<li>")
                    .append("Amount: ").append(payment.getAmount())
                    .append(", Due Date: ").append(payment.getDueDate())
                    .append("</li>");
        }
        body.append("</ul>")
                .append("<p>Please make the payment at your earliest convenience.</p>");

        try {
            emailService.sendEmailToUser(userEmail, subject, body.toString());
            return ResponseEntity.ok("Reminder email sent successfully.");
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send reminder email: " + e.getMessage());
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<PaymentRecord>> getOverduePayments() {
        List<PaymentRecord> overduePayments = paymentRecordService.getAllOverduePayments();
        return ResponseEntity.ok().body(overduePayments);
    }
}
