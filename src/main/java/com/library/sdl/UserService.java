package com.library.sdl;

import com.library.sdl.notification.Notification;
import com.library.sdl.notification.NotificationRepository;
import com.library.sdl.payment.PaymentRecordRepository;
import com.library.sdl.request.RequestType;
import com.library.sdl.request.UserRequest;
import com.library.sdl.request.UserRequestRepository;
import com.library.sdl.request.UserRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.library.sdl.email.EmailService;

// import javax.management.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
    public class UserService {
    @Autowired
    private EmailService emailSenderService;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Autowired
    private UserRequestRepository userRequestRepository;

    @Autowired
    private UserRequestService userRequestService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        logger.info("Fetching user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User with ID {} not found", id);
                    return new RuntimeException("User not found");
                });
    }

    @Transactional
    public User createUser(User user) {
        logger.info("Creating new user with email: {}", user.getEmail());

        // Newly registered user should remain inactive
        user.setIsRegistered("N");

        // ✅ Set default role
        user.setRole("USER");

        // ✅ Enable user
        user.setEnabled(true);

        // Save user FIRST so it gets a valid ID
        User savedUser = userRepository.save(user);

        // Notify admin that a new student registered
        Notification n = new Notification();
        n.setMessage("New student registered: " + savedUser.getName());
        n.setCreatedAt(LocalDateTime.now());
        n.setRead(false);
        notificationRepository.save(n);
        messagingTemplate.convertAndSend("/topic/notifications", n);

        // Create activation request for newly registered user
        userRequestService.createRequest(
                savedUser.getId(),
                RequestType.ACTIVATION,
                "Account activation required"
        );

        // Keep this as your final return
        return savedUser;
    }


    @Transactional
    public User toggleActivateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User or user ID cannot be null");
        }
        String currentStatus = user.getIsRegistered();
        String newStatus = "Y".equals(currentStatus) ? "N" : "Y";
        user.setIsRegistered(newStatus);
        String subject;
        String body;

        if ("N".equals(newStatus)) {
            subject = "Account Deactivated ❌ by SDL";
            body = "Dear " + user.getName() + ",\n\n"
                    + "Your account has been *deactivated* temporarily.\n\n"
                    + "Please clear your due. Ignore if already paid\n"
                    + "Visit: https://shastradigitallibrary.com/\n\n"
                    + "Your Enrollment ID: " + user.getId() + "\n\n"
                    + "Thank you,\nTeam SDL";
        } else {

            subject = "Welcome back to SDL 🎉";
            body = "Dear " + user.getName() + ",\n\n"
                    + "Your account has been *activated* successfully!\n\n"
                    + "Please login and pay your fee at: https://manage.shastradigitallibrary.com/login/\n\n"
                    + "Your Enrollment ID: " + user.getId() + "\n\n"
                    + "Your Password = " + user.getPassword()  + "\n\n"
                    + "Thank you,\nTeam SDL";
        }

        // Send email
        emailSenderService.sendEmailToUser(user.getEmail(), subject, body);

        logger.info("Toggled user registration status for user ID {} to {}", user.getId(), newStatus);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(User existingUser, User updatedUser) {
        logger.info("Updating user ID {}", existingUser.getId());
        existingUser.setSeat(updatedUser.getSeat());
        existingUser.setShift(updatedUser.getShift());
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setMobile(updatedUser.getMobile());
        existingUser.setExtraHour(updatedUser.getExtraHour());
        existingUser.setAdmissionDate(updatedUser.getAdmissionDate());
        return userRepository.save(existingUser);
    }
    @Transactional
    public void deleteUser(Long id) {
        logger.warn("Deleting user with ID: {}", id);
        paymentRecordRepository.deleteByUserId(id);
        notificationRepository.deleteByUserId(id);
        userRequestRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    public User loginUser(String identifier, String password) {
        logger.info("Attempting login for identifier: {}", identifier);

        Optional<User> userOptional;

        // Check if identifier looks like a numeric ID or an email
        if (identifier.matches("\\d+")) {
            // If it's all digits, treat it as user ID
            Long userId = Long.parseLong(identifier);
            userOptional = userRepository.findById(userId)
                    .filter(user -> user.getPassword().equals(password));
        } else {
            // Otherwise, treat it as email
            userOptional = userRepository.findByEmailAndPassword(identifier, password);
        }

        return userOptional.orElseThrow(() -> {
            logger.error("Login failed for identifier: {}", identifier);
            return new RuntimeException("Invalid credentials for: " + identifier);
        });
    }

    public boolean isEmailUnique(String email) {
        boolean isUnique = !userRepository.existsByEmail(email);
        logger.debug("Checking if email is unique [{}]: {}", email, isUnique);
        return isUnique;
    }

    public boolean isMobileUnique(long mobile) {
        boolean isUnique = !userRepository.existsByMobile(mobile);
        logger.debug("Checking if mobile is unique [{}]: {}", mobile, isUnique);
        return isUnique;
    }

    public User getUserByEmail(String email) {
        logger.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new RuntimeException("User not found");
                });
    }

//    public User loginAdmin(String email, String password) {
//        logger.info("Admin login attempt for: {}", email);
//        if ("admin@library.com".equals(email) && "Admin@123".equals(password)) {
//            User admin = new User();
//            admin.setId(0L);
//            admin.setName("Admin");
//            admin.setEmail(email);
//            admin.setIsRegistered("Y");
//            logger.info("Admin login successful");
//            return admin;
//        } else {
//            logger.error("Invalid admin login attempt for email: {}", email);
//            throw new RuntimeException("Invalid admin credentials");
//        }
//    }

    public User loginAdmin(String email, String password) {

        logger.info("Admin login attempt for: {}", email);

        if ("admin@library.com".equals(email) && "Admin@123".equals(password)) {

            User admin = new User();
            admin.setId(1L); // ❗ must be valid
            admin.setName("Admin");
            admin.setEmail(email);
            admin.setRole("ADMIN");      // ✅ REQUIRED
            admin.setIsRegistered("Y");  // optional
            admin.setEnabled(true);      // ✅ REQUIRED
            admin.setPassword(password);
            logger.info("Admin login successful");
            return admin;
        }

        throw new RuntimeException("Invalid admin credentials");
    }

}
