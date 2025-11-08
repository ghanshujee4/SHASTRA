package com.library.sdl.payment;

import com.library.sdl.User;
import com.library.sdl.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentRecordService {

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Autowired
    private UserRepository userRepo;

    private double calculateShiftAmount(String shift) {
        if (shift == null) return 0.0;
        shift = shift.trim().toUpperCase();

        switch (shift) {
            case "SHIFT1":
            case "1": return 450.0;
            case "SHIFT2":
            case "2": return 500.0;
            case "SHIFT3":
            case "3": return 500.0;
            case "SHIFT4":
            case "4": return 600.0;
            case "SHIFT5":
            case "5": return 600.0;
            case "SHIFT1+2":
            case "1,2": return 800.0;
            case "SHIFT2+3":
            case "2,3": return 800.0;
            case "SHIFT1+3":
            case "1,3": return 700.0;
            case "SHIFT1+2+3":
            case "1,2,3": return 1000.0;
            case "SHIFT4+5":
            case "4,5": return 750.0;
            default: return 0.0;
        }
    }

    public void createMonthlyPayment(Long userId, String comments) {
        User user = userRepo.findById(userId).orElseThrow();
        if (user != null) {
            PaymentRecord payment = new PaymentRecord();
            payment.setUser(user);
            double amount = calculateShiftAmount(user.getShift());
            payment.setAmount(amount);
            payment.setComments(comments);
            LocalDate admissionDate = user.getAdmissionDate();
            payment.setDueDate(admissionDate != null ? admissionDate : LocalDate.now());
            payment.setPaid(false);
            payment.setMonthPaid(user.getAdmissionDate());
            paymentRecordRepository.save(payment);
        }
    }

    public List<PaymentRecord> getUserPayments(Long userId) {
        return paymentRecordRepository.findByUserId(userId);
    }

    public void markAsPaid(Long paymentId, Double amount, String comments) {
        PaymentRecord payment = paymentRecordRepository.findById(paymentId).orElseThrow();
        payment.setPaid(true);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDate.now());
        payment.setComments(comments);

        paymentRecordRepository.save(payment);

        PaymentRecord newPayment = new PaymentRecord();
        newPayment.setUser(payment.getUser());
        newPayment.setAmount(amount);
        newPayment.setPaid(false);

        if (payment.getDueDate() != null) {
            LocalDate nextDueDate = payment.getDueDate().plusMonths(1);
            newPayment.setDueDate(nextDueDate);
            newPayment.setMonthPaid(nextDueDate);
        } else {
            LocalDate nextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1);
            newPayment.setDueDate(nextMonth);
            newPayment.setMonthPaid(nextMonth);
        }

        newPayment.setPaymentDate(null);
        newPayment.setComments(null);
        paymentRecordRepository.save(newPayment);
    }

    @Transactional
    public void deleteUser(Long id) {
        paymentRecordRepository.deleteByUserId(id);
    }

    public void updatePayment(Long paymentId, PaymentRecord updatedPayment) {
        PaymentRecord existingPayment = paymentRecordRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        existingPayment.setPaymentDate(updatedPayment.getPaymentDate());
        existingPayment.setAmount(updatedPayment.getAmount());
        existingPayment.setPaid(updatedPayment.getPaid());
        existingPayment.setDueDate(updatedPayment.getDueDate());
        existingPayment.setComments(updatedPayment.getComments());
        existingPayment.setMonthPaid(updatedPayment.getDueDate());
        paymentRecordRepository.save(existingPayment);
    }

    public void deletePayment(Long paymentId) {
        PaymentRecord existingPayment = paymentRecordRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        paymentRecordRepository.delete(existingPayment);
    }

    // ✅ NEW METHOD: Add new manual payment record
    public PaymentRecord addPaymentRecord(Long userId, PaymentRecord record) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PaymentRecord newPayment = new PaymentRecord();
        newPayment.setUser(user);

        // if amount not provided, auto-fill based on shift
        double amount = (record.getAmount() != null && record.getAmount() > 0)
                ? record.getAmount()
                : calculateShiftAmount(user.getShift());
        newPayment.setAmount(amount);

        newPayment.setPaid(false);
        newPayment.setDueDate(record.getDueDate() != null ? record.getDueDate() : LocalDate.now());
        newPayment.setMonthPaid(newPayment.getDueDate());
        newPayment.setComments(
                (record.getComments() != null && !record.getComments().isEmpty())
                        ? record.getComments()
                        : "Manual Payment Entry"
        );

        newPayment.setPaymentDate(null);
        return paymentRecordRepository.save(newPayment);
    }

    public List<PaymentRecord> getOverduePayments(Long userId) {
        List<PaymentRecord> payments = paymentRecordRepository.findByUserId(userId);
        return payments.stream()
                .filter(payment -> payment.getDueDate().isBefore(LocalDate.now()) && !payment.getPaid())
                .collect(Collectors.toList());
    }

    public List<PaymentRecord> getAllOverduePayments() {
        List<PaymentRecord> allPayments = paymentRecordRepository.findAll();
        return allPayments.stream()
                .filter(payment -> {
                    LocalDate dueDate = payment.getDueDate();
                    Boolean paid = payment.getPaid();
                    return dueDate != null && !dueDate.isAfter(LocalDate.now()) && Boolean.FALSE.equals(paid);
                })
                .collect(Collectors.toList());
    }
}
