package com.library.sdl.request;

import com.library.sdl.User;
import com.library.sdl.UserRepository;
import com.library.sdl.notification.Notification;
import com.library.sdl.notification.NotificationRepository;
import com.library.sdl.email.EmailService;
import com.library.sdl.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
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
    private UserRepository userRepo;

    @Autowired
    private NotificationRepository notificationRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Value("${spring.mail.username}")
    private String senderEmailUsername;

    // ✅ Create a new user request (shift/seat change or deactivation)
    public UserRequest createRequest(Long userId, RequestType type, String details) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("❌ User not found with ID: " + userId));

        // Create and save the request
        UserRequest req = new UserRequest();
        req.setUser(user);
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
    public UserRequest approveRequest(Long requestId) {
        UserRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("❌ Request not found with ID: " + requestId));

        req.setStatus("APPROVED");
        User user = req.getUser();

        // 💤 Handle Deactivation Request
        if (req.getType() == RequestType.DEACTIVATION) {
            user.setIsRegistered("N");
            userRepo.save(user);
            notificationService.createAndSend(
                    "✅ Seat/Shift change approved for " + user.getName() + " (Seat " + user.getSeat() + ", Shift " + user.getShift() + ")"
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

        // ✅ Reactivation request
        if (req.getType() == RequestType.REACTIVATION) {
            user.setIsRegistered("Y");
            userRepo.save(user);
            notificationService.createAndSend(
                    "✅ Seat/Shift change approved for " + user.getName() + " (Seat " + user.getSeat() + ", Shift " + user.getShift() + ")"
            );
            emailService.sendEmailToUser(
                    user.getEmail(),
                    "Account Reactivation Approved",
                    """
                    Dear %s,
            
                    Your reactivation request has been approved.
                    Your account is now active again. Welcome back!
                    Pay the Fee and send screenshot to SDL WhatsApp to get it approved.
                    
                    Thank you,
                    Team SDL
                    7979070385
                    """.formatted(user.getName())
            );
        }

        // 🔄 Handle Seat / Shift Change Request
        if (req.getType() == RequestType.SEAT_SHIFT) {
            try {
                // ✅ Example expected format in DB: "Shift change request to [1,2] -> seat:3"
                String details = req.getDetails();

                // Pattern: extract items inside brackets [1,2] and seat after "seat:"
                Pattern pattern = Pattern.compile("\\[(.*?)\\]\\s*->\\s*seat:(\\d+)");
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
                    notificationService.createAndSend(
                            "✅ Seat/Shift change approved for " + user.getName() + " (Seat " + user.getSeat() + ", Shift " + user.getShift() + ")"
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

        return requestRepo.save(req);
    }

    // ❌ Reject a request
    public UserRequest rejectRequest(Long requestId) {
        UserRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("❌ Request not found with ID: " + requestId));

        req.setStatus("REJECTED");
        requestRepo.save(req);

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

        return req;
    }
}