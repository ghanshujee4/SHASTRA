package com.library.sdl.request;

import com.library.sdl.idCard.IdCardService;
import com.library.sdl.payment.PaymentRecord;
import com.library.sdl.payment.PaymentRecordRepository;
import com.library.sdl.User;
import com.library.sdl.UserRepository;
import com.library.sdl.notification.Notification;
import com.library.sdl.notification.NotificationRepository;
import com.library.sdl.email.EmailService;
// import com.library.sdl.idCard.IdCardSerice;
import com.library.sdl.payment.PaymentRecordService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.library.sdl.notification.NotificationService;
//import com.library.sdl.notification.NotificationRepository;

@Service
public class UserRequestService {

    @Autowired
    private UserRequestRepository requestRepo;

    @Autowired
    private PaymentRecordService paymentRecordService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private NotificationRepository notificationRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Autowired
    private IdCardService idCardService;

    @Autowired
    private NotificationService notificationService;

    @Value("${spring.mail.username}")
    private String senderEmailUsername;

    private static final Pattern PAYMENT_PATTERN =
            Pattern.compile("paymentId=(\\d+),type=(\\w+)");

    private PaymentRecord extractPaymentFromDetails(String details) {
        Matcher matcher = PAYMENT_PATTERN.matcher(details);

        if (!matcher.find()) {
            throw new RuntimeException("Invalid payment details format");
        }

        Long paymentId = Long.parseLong(matcher.group(1));

        return paymentRecordRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    private String extractPaymentType(String details) {
        Matcher matcher = PAYMENT_PATTERN.matcher(details);

        if (!matcher.find()) {
            return "UNKNOWN";
        }

        return matcher.group(2);
    }

    // ✅ Create a new user request (shift/seat change or deactivation)
    public UserRequest createRequest(Long userId, RequestType type, String details) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("❌ User not found with ID: " + userId));

        // Create and save the request
        UserRequest req = new UserRequest();
        req.setUser(user);
        req.setCreatedAt(LocalDateTime.now());
        req.setType(type);
        req.setDetails(details);
        req.setStatus("PENDING");
        req.setCreatedAt(LocalDateTime.now());
        requestRepo.save(req);
        // 🔔 Create admin notification
        Notification n = new Notification();
        n.setMessage("📩 New " + type + " request from " + user.getName());
        n.setCreatedAt(LocalDateTime.now());
        n.setRead(false);
        notificationRepo.save(n);
        if (type == RequestType.REACTIVATION || type == RequestType.SEAT_SHIFT) {

            paymentRecordService.reactivationShiftSeatChangePayment(
                    userId,
                    type == RequestType.REACTIVATION
                            ? "Reactivation Request Fee"
                            : "Seat / Shift Change Request Fee"
            );
        }

        if (type == RequestType.PAYMENT_APPROVAL) {

            PaymentRecord payment = extractPaymentFromDetails(details);
            String paymentType = extractPaymentType(details);

            payment.setComments("User claimed paid via " + paymentType);
            paymentRecordRepository.save(payment);

            notificationService.createAndSend(
                    "💰 Payment request raised by " + user.getName()
                            + " | ₹" + payment.getAmount()
                            + " | Mode: " + paymentType
            );
        }
        // ✉️ Send email notifications
        try {
            String adminEmail = senderEmailUsername;
            String adminSubject = "📩 New " + type + " Request from " + user.getName();
            String adminBody = String.format(
                    """
                    Dear Admin,

                    A new user request has been submitted.

                    👤 Name: %s
                    🆔 Enrollment ID: %d
                    📧 Email: %s
                    🪪 Type: %s
                    📝 Details: %s
                    ⏰ Date: %s

                    Please review this in the Admin Dashboard.
                    """,
                    user.getName(),
                    user.getId(),
                    user.getEmail(),
                    type,
                    details,
                    LocalDateTime.now()
            );

            // Send email to admin
            emailService.sendEmail(adminEmail, adminSubject, adminBody);

            // Confirmation email to user
            String userSubject = "✅ Your Request Has Been Received";
            String userBody = String.format(
                    """
                    Dear %s,

                    Your %s request has been successfully submitted.

                    📝 Details: %s
                    ⏰ Date: %s

                    You will receive another email once it is Approved/Rejected by the admin.

                    Thank you,
                    Team SDL
                    """,
                    user.getName(),
                    type,
                    details,
                    LocalDateTime.now()
            );

            emailService.sendEmailToUser(user.getEmail(), userSubject, userBody);

        } catch (Exception e) {
            System.err.println("⚠️ Email sending failed (request saved successfully): " + e.getMessage());
        }

        System.out.println("✅ New request created for user ID " + userId);
        return req;
    }

    // ✅ Fetch all requests (for admin dashboard)
    public List<UserRequest> getAllRequests() {
        return requestRepo.findAll();
    }

    // ✅ Fetch requests by user
    public List<UserRequest> getUserRequests(Long userId) {
        return requestRepo.findByUserId(userId);
    }

    // ✅ Approve a request
    @Transactional
    public UserRequest approveRequest(Long requestId) throws MessagingException {

        UserRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("❌ Request not found with ID: " + requestId));

        req.setStatus("APPROVED");
        User user = req.getUser();

        // 💤 Handle Deactivation Request
        if (req.getType() == RequestType.DEACTIVATION) {
            user.setIsRegistered("N");
            userRepo.save(user);
            notificationService.createAndSendToUser(
                    user.getId(),
                    "✅ Your deactivation request has been approved"
            );
            emailService.sendEmailToUser(
                    user.getEmail(),
                    "Account Deactivation Approved",
                    String.format(
                            """
                            Dear %s,

                            Your deactivation request has been approved.
                            Your account is now temporarily inactive.

                            Please contact the admin if this was not intended.

                            Thank you,
                            Team SDL
                            """,
                            user.getName()
                    )
            );
        }
        // ✅ Handle Activation Request for newly registered users
        if (req.getType() == RequestType.ACTIVATION) {

            user.setIsRegistered("Y"); // Activate user
            userRepo.save(user);
            PaymentRecord payment =
                    paymentRecordService.getLatestPaidPayment(user.getId());

            byte[] pdf = idCardService.generateIdCard(
                    user,
                    payment.getDueDate()
            );

            notificationService.createAndSendToUser(
                    user.getId(),
                    "🎉 Your account has been activated! Welcome to SDL"
            );

            emailService.sendEmailToUser(
                    user.getEmail(),
                    "Your SDL Account is Activated 🎉",
                    String.format("""
                    Dear %s,
                    
                    Your SDL account activation request has been approved.
                    You can now log in and use the digital library services.

                    Thank you,
                    Team SDL
                    """, user.getName())
            );
        }

        // ✅ Reactivation request
        if (req.getType() == RequestType.REACTIVATION) {
            user.setIsRegistered("Y");
            userRepo.save(user);
            notificationService.createAndSendToUser(
                    user.getId(),
                    "✅ Your account has been reactivated! Welcome back"
            );
            emailService.sendEmailToUser(
                    user.getEmail(),
                    "Account Reactivation Approved",
                    """
                    Dear %s,
            
                    Your reactivation request has been approved.
                    Your account is now active again. Welcome back!
                    
                    Thank you,
                    Team SDL
                    """.formatted(user.getName())
            );
        }

        // 🔄 Handle Seat / Shift Change Request
        if (req.getType() == RequestType.SEAT_SHIFT) {

            try {
                // ✅ Example expected format in DB: "Shift change request to [1,2] -> seat:3"
                String details = req.getDetails();

                // Pattern: extract items inside brackets [1,2] and seat after "seat:"
                Pattern pattern = Pattern.compile("\\[(.*?)]\\s*->\\s*seat:(\\d+)");
                Matcher matcher = pattern.matcher(details);

                if (matcher.find()) {
                    String shiftPart = matcher.group(1).trim(); // e.g. "1,2"
                    String seatPart = matcher.group(2).trim();  // e.g. "3"

                    // ✅ Remove extra spaces if any
                    shiftPart = shiftPart.replaceAll("\\s+", "");

                    System.out.printf("Updating user ID %d → Shifts=%s, Seat=%s%n",
                            user.getId(), shiftPart, seatPart);

                    user.setShift(shiftPart);
                    user.setSeat(seatPart);
                    userRepo.save(user);
                    notificationService.createAndSendToUser(
                            user.getId(),
                            "✅ Your seat/shift change has been approved (Seat " + user.getSeat() + ", Shift " + user.getShift() + ")"
                    );
                    // ✅ Send confirmation email
                    emailService.sendEmailToUser(
                            user.getEmail(),
                            "Seat/Shift Change Approved",
                            String.format(
                                    """
                                    Dear %s,
        
                                    Your seat/shift change request has been approved.
        
                                    ✅ New Shift(s): %s
                                    ✅ New Seat: %s
        
                                    Please check your dashboard for updated details.
        
                                    Thank you,
                                    Team SDL
                                    """,
                                    user.getName(),
                                    shiftPart,
                                    seatPart
                            )
                    );

                } else {
                    System.err.println("⚠️ Invalid SEAT_SHIFT format: " + details);
                }

            } catch (Exception e) {
                System.err.println("❌ Error updating shift/seat: " + e.getMessage());
            }
        }

        if (req.getType() == RequestType.PAYMENT_APPROVAL) {

            PaymentRecord payment = extractPaymentFromDetails(req.getDetails());
            String paymentType = extractPaymentType(req.getDetails());

            paymentRecordService.markAsPaid(
                    payment.getId(),
                    payment.getAmount(),
                    "Approved (" + paymentType + ")"
            );
            // 🔔 notification - user-specific
            notificationService.createAndSendToUser(
                    payment.getUser().getId(),
                    "✅ Your payment of ₹" + payment.getAmount() + " has been approved"
            );

            // ✉️ email
            emailService.sendEmailToUser(
                    payment.getUser().getEmail(),
                    "Payment Approved ✅",
                    String.format("""
            Dear %s,

            Your payment of ₹%.2f has been successfully verified and approved.

            Mode: %s

            Thank you,
            Team SDL
            """,
                            payment.getUser().getName(),
                            payment.getAmount(),
                            paymentType
                    )
            );
        }

        return requestRepo.save(req);
    }


    // ❌ Reject a request
    public UserRequest rejectRequest(Long requestId) {
        UserRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("❌ Request not found with ID: " + requestId));

        req.setStatus("REJECTED");
        requestRepo.save(req);

        // 🔔 Push WebSocket notification to user + admin
        notificationService.createAndSendToUser(
                req.getUser().getId(),
                "❌ Your " + req.getType() + " request has been rejected"
        );

        emailService.sendEmailToUser(
                req.getUser().getEmail(),
                "Request Rejected",
                String.format(
                        """
                        Dear %s,

                        Your request (%s) has been reviewed and rejected by the admin.

                        Details: %s

                        Thank you for your understanding,
                        Team SDL
                        """,
                        req.getUser().getName(),
                        req.getType(),
                        req.getDetails()
                )
        );

        if (req.getType() == RequestType.PAYMENT_APPROVAL) {

            PaymentRecord payment = extractPaymentFromDetails(req.getDetails());

            payment.setComments("Payment request rejected");
            paymentRecordRepository.save(payment);

            notificationService.createAndSendToUser(
                    payment.getUser().getId(),
                    "❌ Your payment request has been rejected"
            );
        }
        return req;
    }

    // ❌ Delete a request
    @Transactional
    public void deleteRequest(Long requestId) {
        UserRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("❌ Request not found with ID: " + requestId));

        User user = req.getUser();
        RequestType type = req.getType();
        String details = req.getDetails();

        System.out.println("🗑️ Deleting request ID " + requestId + " for user " + user.getName());

        // ✉️ Send notification email to user
        try {
            emailService.sendEmailToUser(
                    user.getEmail(),
                    "Request Deleted",
                    String.format(
                            """
                            Dear %s,
                            
                            Your %s request (ID: %d) has been deleted.
                            
                            Details: %s
                            
                            If you believe this was done in error, please contact support.
                            
                            Thank you,
                            Team SDL
                            """,
                            user.getName(),
                            type,
                            requestId,
                            details
                    )
            );
        } catch (Exception e) {
            System.err.println("⚠️ Email notification failed: " + e.getMessage());
        }
        
        // 🔔 Push WebSocket notification to user + admin with real-time updates
        notificationService.createAndSendToUser(
                user.getId(),
                "🗑️ Your " + type + " request (ID: " + requestId + ") has been deleted"
        );

        // Delete the request
        requestRepo.deleteById(requestId);
        System.out.println("✅ Request ID " + requestId + " deleted successfully");
    }
}

