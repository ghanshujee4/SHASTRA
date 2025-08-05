package com.library.sdl.payment;

import com.library.sdl.User;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "payment_records")
public class PaymentRecord {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;
    //private Long userId;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private Double amount;
    private Boolean isPaid;
    private LocalDate monthPaid;
    private String comments;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDate getMonthPaid() {
        return monthPaid;
    }

    public void setMonthPaid(LocalDate monthPaid) {
        this.monthPaid = monthPaid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Boolean getPaid() {
        return isPaid;
    }

    public void setPaid(Boolean paid) {
        isPaid = paid;
    }
}
