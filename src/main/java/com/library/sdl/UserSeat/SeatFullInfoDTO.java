package com.library.sdl.UserSeat;

import java.time.LocalDate;

public class SeatFullInfoDTO {
    private int seatNo;

    // User details
    private Long userId;
    private String userName;
    private String email;
    private Long mobile;
    private String shift;
    private String address;

    // Payment details
    private Boolean isPaid;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private Double amount;
    private String comments;

    public int getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(int seatNo) {
        this.seatNo = seatNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getMobile() {
        return mobile;
    }

    public void setMobile(Long mobile) {
        this.mobile = mobile;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getPaid() {
        return isPaid;
    }

    public void setPaid(Boolean paid) {
        isPaid = paid;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    // Constructor
    public SeatFullInfoDTO(int seatNo, Long userId, String userName, Long mobile, String shift,
                           Boolean isPaid, LocalDate dueDate) {
        this.seatNo = seatNo;
        this.userId = userId;
        this.userName = userName;

        this.mobile = mobile;
        this.shift = shift;
        this.isPaid = isPaid;
        this.dueDate = dueDate;

    }

    // Getters and Setters (or use Lombok @Data)
    // ...
}

