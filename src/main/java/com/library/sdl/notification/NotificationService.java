package com.library.sdl.notification;

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

    // 🔔 Create and push notification to admin via WebSocket
    public void createAndSend(String message) {
        Notification n = new Notification();
        n.setMessage(message);
        n.setCreatedAt(LocalDateTime.now());
        n.setRead(false);

        // Save to DB
        notificationRepo.save(n);

        // Broadcast to WebSocket topic
        messagingTemplate.convertAndSend("/topic/notifications", n);

        System.out.println("📢 Notification Sent: " + message);
    }
}
