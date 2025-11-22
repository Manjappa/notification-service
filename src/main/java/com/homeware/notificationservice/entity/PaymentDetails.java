package com.homeware.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;
    
    @Column(name = "merchant_email", nullable = false)
    private String merchantEmail;
    
    @Column(name = "merchant_name", nullable = false)
    private String merchantName;
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", nullable = false, length = 10)
    private String currency;
    
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;
    
    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus; // "SUCCESS" or "FAILED"
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Column(name = "customer_email")
    private String customerEmail;
    
    @Column(name = "customer_name")
    private String customerName;
    
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
    
    @Column(name = "order_id")
    private String orderId;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

