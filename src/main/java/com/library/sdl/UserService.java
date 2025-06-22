package com.library.sdl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        user.setIsRegistered("Y");
        return userRepository.save(user);
    }

    @Transactional
    public User toggleActivateUser(User user) {
        String currentStatus = user.getIsRegistered();
        String newStatus = "Y".equals(currentStatus) ? "N" : "Y";
        user.setIsRegistered(newStatus);
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
        existingUser.setAdmissionDate(updatedUser.getAdmissionDate());
        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        logger.warn("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
    }

    public User loginUser(String email, String password) {
        logger.info("Attempting login for user: {}", email);
        return userRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> {
                    logger.error("Login failed for user: {}", email);
                    return new RuntimeException("User not found: " + email);
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

    public User loginAdmin(String email, String password) {
        logger.info("Admin login attempt for: {}", email);
        if ("admin@library.com".equals(email) && "Admin@123".equals(password)) {
            User admin = new User();
            admin.setId(0L);
            admin.setName("Admin");
            admin.setEmail(email);
            admin.setIsRegistered("Y");
            logger.info("Admin login successful");
            return admin;
        } else {
            logger.error("Invalid admin login attempt for email: {}", email);
            throw new RuntimeException("Invalid admin credentials");
        }
    }
}
