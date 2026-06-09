package com.library.sdl.notification;

import com.library.sdl.idCard.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationService notificationService;

    // ✅ Get my notifications (user-specific) with pagination
    @GetMapping("/my")
    public Page<Notification> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(principal.getId(), pageable);
    }
    
    // ✅ Get unread notification count for current user
    @GetMapping("/my/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        long count = notificationRepository.countByUserIdAndReadFalse(principal.getId());
        return ResponseEntity.ok(count);
    }
    
    // ✅ Get unread notifications for current user
    @GetMapping("/my/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        List<Notification> notifications = notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(principal.getId());
        return ResponseEntity.ok(notifications);
    }
    
    // ✅ Mark notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        Notification n = notificationService.markAsRead(id);
        return ResponseEntity.ok(n);
    }

    // ✅ Get all notifications (admin only)
    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public Notification createNotification(@RequestBody Notification notification) {
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }
}


