package com.library.sdl.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByOrderByCreatedAtDesc();

    // ✅ Find notifications for a specific user with pagination
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // ✅ Find unread notifications for a specific user
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);
    
    // ✅ Count unread notifications for a user
    long countByUserIdAndReadFalse(Long userId);

    Notification save(Notification notification);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

}


