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

    public void createMonthlyPayment(Long userId, Double amount, String comments) {
        User user = userRepo.findById(userId).orElseThrow();
        if (null != user) {
            PaymentRecord payment = new PaymentRecord();
            payment.setUser(user);
            payment.setAmount(amount);
            payment.setComments(comments);
            payment.setDueDate(LocalDate.from(user.getAdmissionDate()));
            if (amount == 0) {
                payment.setPaid(false);
            } else {
                payment.setPaid(true);
            }
            payment.setMonthPaid(user.getAdmissionDate());
            paymentRecordRepository.save(payment);
        }
    }

    public List<PaymentRecord> getUserPayments(Long userId) {
        return paymentRecordRepository.findByUserId(userId);
    }


    public void markAsPaid(Long paymentId, Double amount, String comments) {
        PaymentRecord payment = paymentRecordRepository.findById(paymentId).orElseThrow();
        // payment.setDueDate(LocalDate.from(payment.getDueDate()));
        // Mark current payment as paid
        payment.setPaid(true);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDate.now());
        payment.setComments(comments);
        // Save current payment update
        paymentRecordRepository.save(payment);
        // Create a new payment row for the next month
        PaymentRecord newPayment = new PaymentRecord();
        newPayment.setUser(payment.getUser());
        newPayment.setAmount(amount); // Keep the same amount
        newPayment.setPaid(false);
        newPayment.setMonthPaid(payment.getMonthPaid().plusMonths(1)); // Next month
        newPayment.setDueDate(payment.getDueDate().plusMonths(1)); // New due date with null check
        newPayment.setPaymentDate(null);
        newPayment.setComments(comments);
        // Save new payment record
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
        paymentRecordRepository.save(existingPayment);
    }

    public void deletePayment(Long paymentId) {
        PaymentRecord existingPayment = paymentRecordRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        paymentRecordRepository.delete(existingPayment);
    }

    // Method to get overdue payments
    public List<PaymentRecord> getOverduePayments(Long userId) {
        List<PaymentRecord> payments = paymentRecordRepository.findByUserId(userId);

        // Filter for overdue payments that are still pending
        return payments.stream()
                .filter(payment -> payment.getDueDate().isBefore(LocalDate.now()) && !payment.getPaid())
                .collect(Collectors.toList());
    }

    public List<PaymentRecord> getAllOverduePayments() {
        List<PaymentRecord> allPayments = paymentRecordRepository.findAll();
        System.out.println("Fetched payments count: " + allPayments.size());
        List<PaymentRecord> overduePayments = allPayments.stream()
                .filter(payment -> {
                    LocalDate dueDate = payment.getDueDate();
                    Boolean paid = payment.getPaid();
                    System.out.printf("Checking payment: dueDate=%s, paid=%s%n", dueDate, paid);
                    return dueDate != null && dueDate.isBefore(LocalDate.now()) && Boolean.FALSE.equals(paid);
                })
                .collect(Collectors.toList());

        return overduePayments;
    }
}
