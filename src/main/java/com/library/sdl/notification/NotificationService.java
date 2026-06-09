package com.library.sdl.notification;

import com.library.sdl.User;
import com.library.sdl.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private UserRepository userRepository;

    // 🔔 Create and push notification to specific user + admin via WebSocket
    public void createAndSendToUser(Long userId, String message) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        
        Notification n = new Notification();
        n.setMessage(message);
        n.setUser(user);  // Link to specific user
        n.setCreatedAt(LocalDateTime.now());
        n.setRead(false);

        // Save to DB
        notificationRepo.save(n);

        // Real-time: Push to user's WebSocket topic (if user is specified)
        if (userId != null) {
            messagingTemplate.convertAndSend("/topic/notifications/user/" + userId, n);
        }
        
        // Real-time: Also push to admin dashboard
        messagingTemplate.convertAndSend("/topic/notifications/admin", n);

        System.out.println("📢 Notification Sent to User " + userId + ": " + message);
    }

    // 🔔 Create and push notification to admin only (legacy method)
    public void createAndSend(String message) {
        Notification n = new Notification();
        n.setMessage(message);
        n.setCreatedAt(LocalDateTime.now());
        n.setRead(false);

        // Save to DB
        notificationRepo.save(n);

        // Broadcast to admin WebSocket topic
        messagingTemplate.convertAndSend("/topic/notifications/admin", n);

        System.out.println("📢 Notification Sent (Admin): " + message);
    }
    
    // ✅ Mark notification as read
    public Notification markAsRead(Long notificationId) {
        Notification n = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        return notificationRepo.save(n);
    }
}


