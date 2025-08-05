package com.library.sdl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.sdl.payment.EmailService;
import com.library.sdl.payment.PaymentRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping({ "/api/users"})
public class UserController {
    @Autowired
    private EmailService emailSenderService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private PaymentRecordService paymentRecordService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.info("Fetching user with ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> createUser(
            @RequestPart("adharCard") MultipartFile adharCard,
            @RequestPart("user") String userJson
    ) {
        try {
            logger.info("Received request to create user");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            User user = objectMapper.readValue(userJson, User.class);
            // Save file
            String filePath = fileStorageService.saveFile(adharCard);
            user.setAdharCard(filePath);
            user.setRegistrationDate(LocalDateTime.now());
//            emailSenderService.sendEmail(
//                    user.getEmail(),
//                    "Welcome to SDL",
//                    "Dear " + user.getName() + ",\n\nThanks for registering at Shastra Digital Library."
//            );
            if (user.getShift() == null || user.getShift().isEmpty()) {
                logger.warn("User creation failed: Shift is empty");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            User savedUser = userService.createUser(user);
            paymentRecordService.createMonthlyPayment(savedUser.getId(), 0.0,"");
            logger.info("User created successfully with ID: {}", savedUser.getId());
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}")
    public ResponseEntity<User> toggleActivateUser(@PathVariable Long id) {
        logger.info("Toggling activation for user with ID: {}", id);
        User existingUser = userService.getUserById(id);
        User updatedUser = userService.toggleActivateUser(existingUser);
        logger.info("User activation status updated for ID: {}", id);
        return new ResponseEntity<>(updatedUser, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with ID: {}", id);
        paymentRecordService.deleteUser(id);
        userService.deleteUser(id);
        return "User deleted successfully.";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public LoginResponse loginUser(@RequestBody User user) {
        logger.info("Login attempt for user: {}", user.getEmail());
        User userDto = userService.loginUser(user.getEmail(), user.getPassword());
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setMessage("Login Successful");
        loginResponse.setToken(userDto.getId().toString());
        loginResponse.setUserId(userDto.getId().toString());
        loginResponse.setRole("user");

        logger.info("User logged in successfully: {}", user.getEmail());
        return loginResponse;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public LoginResponse logoutUser() {
        logger.info("User logged out");
        return new LoginResponse();
    }

    @GetMapping("/check-email")
    public UniqueEmailMobileResponse checkEmail(@RequestParam String email) {
        logger.info("Checking email uniqueness: {}", email);
        boolean isUnique = userService.isEmailUnique(email);
        return new UniqueEmailMobileResponse(
                isUnique ? "Email is unique" : "Email already exists", isUnique
        );
    }

    @GetMapping("/check-mobile")
    public UniqueEmailMobileResponse checkMobile(@RequestParam Long mobile) {
        logger.info("Checking mobile uniqueness: {}", mobile);
        boolean isUnique = userService.isMobileUnique(mobile);
        return new UniqueEmailMobileResponse(
                isUnique ? "Mobile number is unique" : "Mobile number already exists", isUnique
        );
    }

    @RequestMapping(value = "/admin/login", method = RequestMethod.POST)
    public LoginResponse adminLogin(@RequestBody User user) {
        try {
            logger.info("Admin login attempt: {}", user.getEmail());
            User admin = userService.loginAdmin(user.getEmail(), user.getPassword());

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setMessage("Admin Login Successful");
            loginResponse.setToken("ADMIN-TOKEN-" + admin.getId());
            loginResponse.setUserId(admin.getId().toString());
            loginResponse.setRole("admin");  // Set the role for future use
            logger.info("Admin logged in successfully: {}", user.getEmail());
            return loginResponse;
        } catch (RuntimeException e) {
            logger.error("Admin login failed for: {}", user.getEmail(), e);
            return null;
        }
    }

    @GetMapping("/admin-only")
    public ResponseEntity<String> adminOnly(@RequestHeader("Authorization") String token) {
        logger.info("Admin access attempt with token: {}", token);
        if (token != null && token.startsWith("ADMIN-TOKEN")) {
            return ResponseEntity.ok("Welcome, Admin!");
        }
        logger.warn("Unauthorized admin access attempt");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        logger.info("Updating user with ID: {}", id);
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            logger.warn("User not found with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User updatedUserData = userService.updateUser(existingUser, updatedUser);
        logger.info("User updated successfully with ID: {}", id);
        return new ResponseEntity<>(updatedUserData, HttpStatus.OK);
    }
}
