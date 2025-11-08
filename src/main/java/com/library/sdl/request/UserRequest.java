package com.library.sdl.request;

import com.library.sdl.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class UserRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private RequestType type; // DEACTIVATION, SEAT_SHIFT

    private String details; // e.g. "Request to shift from SHIFT1 to SHIFT2"
    private String status = "PENDING"; // PENDING / APPROVED / REJECTED
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public RequestType getType() { return type; }
    public void setType(RequestType type) { this.type = type; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
