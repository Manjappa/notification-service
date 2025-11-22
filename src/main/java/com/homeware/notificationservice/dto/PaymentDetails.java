package com.homeware.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetails {
    
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    
    @NotBlank(message = "Merchant email is required")
    private String merchantEmail;
    
    @NotBlank(message = "Merchant name is required")
    private String merchantName;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    @NotBlank(message = "Payment status is required")
    private String paymentStatus; // "SUCCESS" or "FAILED"
    
    private String failureReason; // Required only when paymentStatus is "FAILED"
    
    private String customerEmail;
    private String customerName;
    private LocalDateTime transactionDate;
    private String orderId;
    private String description;
}

