package com.library.sdl;


import com.fasterxml.jackson.annotation.JsonFormat;
// import com.library.sdl.emailverification.VerificationToken;
import jakarta.persistence.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "student_records")
public class User{

    @Id
    //@Column(name  = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "age")
    private String age;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Column(name = "role")
    private String role;

    @Column(name = "mobile", nullable = false, unique = true)
    private Long mobile;

    @Column(name = "address")
    private String address;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "shift")
    private String shift;

    @Column(name = "seat")
    private String seat;

    @Column(name = "adhar")
    private String adhar;

    @Column(name = "password")
    private String password;

    @Column(name = "is_registered")
    private String isRegistered;

    @Column(name = "adharCard")
    private String adharCard; // Store file path or URL here

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;  // New field for registration date

    @Column(name = "admission_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate admissionDate;

    @Column(name = "extraHour")
    private String extraHour;

    public String getExtraHour() {
        return extraHour;
    }

    public void setExtraHour(String extraHour) {
        this.extraHour = extraHour;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getAdharCard() {
        return adharCard;
    }

    public void setAdharCard(String adharCard) {
        this.adharCard = adharCard;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public void setMobile(Long mobile) {
        this.mobile = mobile;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(String isRegistered) {
        this.isRegistered = isRegistered;
    }

    public String getAddress() {
        return address;
    }

    public Long getMobile() {
        return mobile;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAge() {
        return age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAdhar() {
        return adhar;
    }

    public void setAdhar(String adhar) {
        this.adhar = adhar;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Column(name = "enabled")
    private boolean enabled;

//    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
//    private VerificationToken verificationToken;
//
//    public VerificationToken getVerificationToken() {
//        return verificationToken;
//    }
//
//    public void setVerificationToken(VerificationToken verificationToken) {
//        this.verificationToken = verificationToken;
//    }
}