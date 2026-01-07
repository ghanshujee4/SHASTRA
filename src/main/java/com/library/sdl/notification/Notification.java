package com.library.sdl.notification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.library.sdl.User;
import com.library.sdl.request.UserRequest;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private boolean read = false;
    private LocalDateTime createdAt;

    // ✅ Default no-args constructor (required by JPA)
    public Notification() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // (Optional) constructor for convenience
    public Notification(String message) {
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }
    // ✅ Getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }


    public boolean isRead() {
        return read;
    }
    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    // ✅ THIS WAS THE BUG
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void getMessage(String s) {
    }
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;   // ✅ correct

}
