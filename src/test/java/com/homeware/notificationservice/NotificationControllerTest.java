package com.homeware.notificationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeware.notificationservice.dto.PaymentDetails;
import com.homeware.notificationservice.repository.PaymentDetailsRepository;
import com.homeware.notificationservice.service.EmailNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.test.web.servlet.MockMvc;
import com.homeware.notificationservice.exception.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.homeware.notificationservice.controller.NotificationController.class)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailNotificationService emailNotificationService;

    @MockBean
    private PaymentDetailsRepository paymentDetailsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPaymentSuccessEndpoint() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123456789", "SUCCESS");
        
        when(paymentDetailsRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        when(paymentDetailsRepository.save(any(com.homeware.notificationservice.entity.PaymentDetails.class)))
                .thenReturn(new com.homeware.notificationservice.entity.PaymentDetails());
        doNothing().when(emailNotificationService).sendPaymentNotification(any(PaymentDetails.class));

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment successful !!!"));
        
        verify(paymentDetailsRepository, times(1)).findByTransactionId("TXN123456789");
        verify(paymentDetailsRepository, times(1)).save(any(com.homeware.notificationservice.entity.PaymentDetails.class));
        verify(emailNotificationService, times(1)).sendPaymentNotification(any(PaymentDetails.class));
    }

    @Test
    void testPaymentFailedEndpoint() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN987654321", "FAILED");
        paymentDetails.setFailureReason("Insufficient funds");
        
        when(paymentDetailsRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        when(paymentDetailsRepository.save(any(com.homeware.notificationservice.entity.PaymentDetails.class)))
                .thenReturn(new com.homeware.notificationservice.entity.PaymentDetails());
        doNothing().when(emailNotificationService).sendPaymentNotification(any(PaymentDetails.class));

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment successful !!!"));
        
        verify(emailNotificationService, times(1)).sendPaymentNotification(any(PaymentDetails.class));
    }

    @Test
    void testPaymentSuccessWithOptionalFields() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN111222333", "SUCCESS");
        paymentDetails.setCustomerEmail("customer@example.com");
        paymentDetails.setCustomerName("John Doe");
        paymentDetails.setOrderId("ORD123456");
        paymentDetails.setDescription("Product purchase");
        
        when(paymentDetailsRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        when(paymentDetailsRepository.save(any(com.homeware.notificationservice.entity.PaymentDetails.class)))
                .thenReturn(new com.homeware.notificationservice.entity.PaymentDetails());
        doNothing().when(emailNotificationService).sendPaymentNotification(any(PaymentDetails.class));

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void testMissingTransactionId() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "SUCCESS");
        paymentDetails.setTransactionId(null);

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isBadRequest());
        
        verify(paymentDetailsRepository, never()).save(any());
        verify(emailNotificationService, never()).sendPaymentNotification(any());
    }

    @Test
    void testMissingMerchantEmail() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "SUCCESS");
        paymentDetails.setMerchantEmail(null);

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidEmailFormat() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "SUCCESS");
        paymentDetails.setMerchantEmail("invalid-email");

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMissingMerchantName() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "SUCCESS");
        paymentDetails.setMerchantName(null);

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMissingAmount() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "SUCCESS");
        paymentDetails.setAmount(null);

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMissingCurrency() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "SUCCESS");
        paymentDetails.setCurrency(null);

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMissingPaymentMethod() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "SUCCESS");
        paymentDetails.setPaymentMethod(null);

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMissingPaymentStatus() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "SUCCESS");
        paymentDetails.setPaymentStatus(null);

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFailedPaymentWithoutFailureReason() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "FAILED");
        paymentDetails.setFailureReason(null);
        
        when(paymentDetailsRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failure reason is required when payment status is FAILED"));
        
        verify(paymentDetailsRepository, never()).save(any());
        verify(emailNotificationService, never()).sendPaymentNotification(any());
    }

    @Test
    void testFailedPaymentWithEmptyFailureReason() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "FAILED");
        paymentDetails.setFailureReason("   ");
        
        when(paymentDetailsRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failure reason is required when payment status is FAILED"));
    }

    @Test
    void testDuplicateTransactionId() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123456789", "SUCCESS");
        com.homeware.notificationservice.entity.PaymentDetails existingEntity = 
                new com.homeware.notificationservice.entity.PaymentDetails();
        existingEntity.setTransactionId("TXN123456789");
        
        when(paymentDetailsRepository.findByTransactionId("TXN123456789"))
                .thenReturn(Optional.of(existingEntity));

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Transaction ID already exists"));
        
        verify(paymentDetailsRepository, never()).save(any());
        verify(emailNotificationService, never()).sendPaymentNotification(any());
    }

    @Test
    void testDatabaseError() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "SUCCESS");
        
        when(paymentDetailsRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        when(paymentDetailsRepository.save(any(com.homeware.notificationservice.entity.PaymentDetails.class)))
                .thenThrow(new DataAccessException("Database connection failed") {});

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Database Error"));
    }

    @Test
    void testEmailServiceError() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "SUCCESS");
        
        when(paymentDetailsRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        when(paymentDetailsRepository.save(any(com.homeware.notificationservice.entity.PaymentDetails.class)))
                .thenReturn(new com.homeware.notificationservice.entity.PaymentDetails());
        doThrow(new MailException("SMTP server unavailable") {})
                .when(emailNotificationService).sendPaymentNotification(any(PaymentDetails.class));

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Email Service Error"));
    }

    @Test
    void testInvalidPaymentStatus() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "PENDING");
        
        when(paymentDetailsRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        when(paymentDetailsRepository.save(any(com.homeware.notificationservice.entity.PaymentDetails.class)))
                .thenReturn(new com.homeware.notificationservice.entity.PaymentDetails());
        doThrow(new IllegalArgumentException("Invalid payment status: PENDING"))
                .when(emailNotificationService).sendPaymentNotification(any(PaymentDetails.class));

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPaymentStatusCaseInsensitive() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "success");
        
        when(paymentDetailsRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        when(paymentDetailsRepository.save(any(com.homeware.notificationservice.entity.PaymentDetails.class)))
                .thenReturn(new com.homeware.notificationservice.entity.PaymentDetails());
        doNothing().when(emailNotificationService).sendPaymentNotification(any(PaymentDetails.class));

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void testEmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidJsonFormat() throws Exception {
        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLargeAmount() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "SUCCESS");
        paymentDetails.setAmount(new BigDecimal("999999999.99"));
        
        when(paymentDetailsRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        when(paymentDetailsRepository.save(any(com.homeware.notificationservice.entity.PaymentDetails.class)))
                .thenReturn(new com.homeware.notificationservice.entity.PaymentDetails());
        doNothing().when(emailNotificationService).sendPaymentNotification(any(PaymentDetails.class));

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void testZeroAmount() throws Exception {
        PaymentDetails paymentDetails = createValidPaymentDetails("TXN123", "SUCCESS");
        paymentDetails.setAmount(BigDecimal.ZERO);
        
        when(paymentDetailsRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        when(paymentDetailsRepository.save(any(com.homeware.notificationservice.entity.PaymentDetails.class)))
                .thenReturn(new com.homeware.notificationservice.entity.PaymentDetails());
        doNothing().when(emailNotificationService).sendPaymentNotification(any(PaymentDetails.class));

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isOk());
    }

    // Helper method to create valid payment details
    private PaymentDetails createValidPaymentDetails(String transactionId, String paymentStatus) {
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setTransactionId(transactionId);
        paymentDetails.setMerchantEmail("merchant@example.com");
        paymentDetails.setMerchantName("Test Merchant");
        paymentDetails.setAmount(new BigDecimal("100.50"));
        paymentDetails.setCurrency("USD");
        paymentDetails.setPaymentMethod("Credit Card");
        paymentDetails.setPaymentStatus(paymentStatus);
        paymentDetails.setTransactionDate(LocalDateTime.now());
        return paymentDetails;
    }
}
