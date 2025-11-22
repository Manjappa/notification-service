package com.homeware.notificationservice.controller;

import com.homeware.notificationservice.dto.PaymentDetails;
import com.homeware.notificationservice.repository.PaymentDetailsRepository;
import com.homeware.notificationservice.service.EmailNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailNotificationService emailNotificationService;
    private final PaymentDetailsRepository paymentDetailsRepository;

    @PostMapping("/payment")
    public ResponseEntity<String> handlePaymentNotification(@Valid @RequestBody PaymentDetails paymentDetails) {
        try {
            log.info("Received payment notification for transaction: {} with status: {}", 
                    paymentDetails.getTransactionId(), paymentDetails.getPaymentStatus());
            
            // Validate failure reason for failed payments
            if ("FAILED".equalsIgnoreCase(paymentDetails.getPaymentStatus()) 
                    && (paymentDetails.getFailureReason() == null || paymentDetails.getFailureReason().trim().isEmpty())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Failure reason is required when payment status is FAILED");
            }
            
            // Check if transaction already exists
            if (paymentDetailsRepository.findByTransactionId(paymentDetails.getTransactionId()).isPresent()) {
                log.warn("Transaction ID {} already exists in database", paymentDetails.getTransactionId());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Transaction ID already exists");
            }
            
            // Convert DTO to Entity and save to database
            com.homeware.notificationservice.entity.PaymentDetails paymentEntity = convertToEntity(paymentDetails);
            paymentDetailsRepository.save(paymentEntity);
            log.info("Payment details saved to database with ID: {}", paymentEntity.getId());
            
            // Send email notification
            emailNotificationService.sendPaymentNotification(paymentDetails);
            
            return ResponseEntity.ok("Payment notification processed and saved successfully");
        } catch (Exception e) {
            log.error("Error processing payment notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process notification: " + e.getMessage());
        }
    }
    
    private com.homeware.notificationservice.entity.PaymentDetails convertToEntity(PaymentDetails dto) {
        com.homeware.notificationservice.entity.PaymentDetails entity = new com.homeware.notificationservice.entity.PaymentDetails();
        entity.setTransactionId(dto.getTransactionId());
        entity.setMerchantEmail(dto.getMerchantEmail());
        entity.setMerchantName(dto.getMerchantName());
        entity.setAmount(dto.getAmount());
        entity.setCurrency(dto.getCurrency());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setPaymentStatus(dto.getPaymentStatus().toUpperCase());
        entity.setFailureReason(dto.getFailureReason());
        entity.setCustomerEmail(dto.getCustomerEmail());
        entity.setCustomerName(dto.getCustomerName());
        entity.setTransactionDate(dto.getTransactionDate());
        entity.setOrderId(dto.getOrderId());
        entity.setDescription(dto.getDescription());
        return entity;
    }
}
