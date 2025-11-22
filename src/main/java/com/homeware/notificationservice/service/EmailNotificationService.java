package com.homeware.notificationservice.service;

import com.homeware.notificationservice.dto.PaymentDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    public void sendPaymentNotification(PaymentDetails paymentDetails) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(paymentDetails.getMerchantEmail());
            
            if ("SUCCESS".equalsIgnoreCase(paymentDetails.getPaymentStatus())) {
                message.setSubject("Payment Success - Transaction " + paymentDetails.getTransactionId());
                message.setText(buildPaymentSuccessEmailBody(paymentDetails));
            } else if ("FAILED".equalsIgnoreCase(paymentDetails.getPaymentStatus())) {
                message.setSubject("Payment Failed - Transaction " + paymentDetails.getTransactionId());
                message.setText(buildPaymentFailedEmailBody(paymentDetails));
            } else {
                throw new IllegalArgumentException("Invalid payment status: " + paymentDetails.getPaymentStatus());
            }
            
            message.setFrom("noreply@homeware.com");
            mailSender.send(message);
            log.info("Payment {} email sent to merchant: {}", paymentDetails.getPaymentStatus(), paymentDetails.getMerchantEmail());
        } catch (Exception e) {
            log.error("Failed to send payment {} email to merchant: {}", paymentDetails.getPaymentStatus(), paymentDetails.getMerchantEmail(), e);
            throw new RuntimeException("Failed to send email notification", e);
        }
    }

    private String buildPaymentSuccessEmailBody(PaymentDetails paymentDetails) {
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(paymentDetails.getMerchantName()).append(",\n\n");
        body.append("We are pleased to inform you that a payment has been successfully processed.\n\n");
        body.append("Transaction Details:\n");
        body.append("-------------------\n");
        body.append("Transaction ID: ").append(paymentDetails.getTransactionId()).append("\n");
        body.append("Amount: ").append(paymentDetails.getAmount()).append(" ").append(paymentDetails.getCurrency()).append("\n");
        body.append("Payment Method: ").append(paymentDetails.getPaymentMethod()).append("\n");
        
        if (paymentDetails.getOrderId() != null && !paymentDetails.getOrderId().isEmpty()) {
            body.append("Order ID: ").append(paymentDetails.getOrderId()).append("\n");
        }
        
        if (paymentDetails.getCustomerName() != null && !paymentDetails.getCustomerName().isEmpty()) {
            body.append("Customer: ").append(paymentDetails.getCustomerName());
            if (paymentDetails.getCustomerEmail() != null && !paymentDetails.getCustomerEmail().isEmpty()) {
                body.append(" (").append(paymentDetails.getCustomerEmail()).append(")");
            }
            body.append("\n");
        }
        
        if (paymentDetails.getTransactionDate() != null) {
            body.append("Transaction Date: ").append(
                    paymentDetails.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ).append("\n");
        }
        
        if (paymentDetails.getDescription() != null && !paymentDetails.getDescription().isEmpty()) {
            body.append("Description: ").append(paymentDetails.getDescription()).append("\n");
        }
        
        body.append("\nThank you for using our payment service.\n\n");
        body.append("Best regards,\n");
        body.append("Homeware Payment System");
        
        return body.toString();
    }

    private String buildPaymentFailedEmailBody(PaymentDetails paymentDetails) {
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(paymentDetails.getMerchantName()).append(",\n\n");
        body.append("We regret to inform you that a payment transaction has failed.\n\n");
        body.append("Transaction Details:\n");
        body.append("-------------------\n");
        body.append("Transaction ID: ").append(paymentDetails.getTransactionId()).append("\n");
        body.append("Amount: ").append(paymentDetails.getAmount()).append(" ").append(paymentDetails.getCurrency()).append("\n");
        body.append("Payment Method: ").append(paymentDetails.getPaymentMethod()).append("\n");
        
        if (paymentDetails.getFailureReason() != null && !paymentDetails.getFailureReason().isEmpty()) {
            body.append("Failure Reason: ").append(paymentDetails.getFailureReason()).append("\n");
        }
        
        if (paymentDetails.getOrderId() != null && !paymentDetails.getOrderId().isEmpty()) {
            body.append("Order ID: ").append(paymentDetails.getOrderId()).append("\n");
        }
        
        if (paymentDetails.getCustomerName() != null && !paymentDetails.getCustomerName().isEmpty()) {
            body.append("Customer: ").append(paymentDetails.getCustomerName());
            if (paymentDetails.getCustomerEmail() != null && !paymentDetails.getCustomerEmail().isEmpty()) {
                body.append(" (").append(paymentDetails.getCustomerEmail()).append(")");
            }
            body.append("\n");
        }
        
        if (paymentDetails.getTransactionDate() != null) {
            body.append("Transaction Date: ").append(
                    paymentDetails.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ).append("\n");
        }
        
        if (paymentDetails.getDescription() != null && !paymentDetails.getDescription().isEmpty()) {
            body.append("Description: ").append(paymentDetails.getDescription()).append("\n");
        }
        
        body.append("\nPlease review the transaction and contact support if needed.\n\n");
        body.append("Best regards,\n");
        body.append("Homeware Payment System");
        
        return body.toString();
    }
}
