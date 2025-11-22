package com.homeware.notificationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeware.notificationservice.dto.PaymentDetails;
import com.homeware.notificationservice.repository.PaymentDetailsRepository;
import com.homeware.notificationservice.service.EmailNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(com.homeware.notificationservice.controller.NotificationController.class)
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
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setTransactionId("TXN123456789");
        paymentDetails.setMerchantEmail("merchant@example.com");
        paymentDetails.setMerchantName("Test Merchant");
        paymentDetails.setAmount(new BigDecimal("100.50"));
        paymentDetails.setCurrency("USD");
        paymentDetails.setPaymentMethod("Credit Card");
        paymentDetails.setPaymentStatus("SUCCESS");
        paymentDetails.setTransactionDate(LocalDateTime.now());

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void testPaymentFailedEndpoint() throws Exception {
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setTransactionId("TXN987654321");
        paymentDetails.setMerchantEmail("merchant@example.com");
        paymentDetails.setMerchantName("Test Merchant");
        paymentDetails.setAmount(new BigDecimal("100.50"));
        paymentDetails.setCurrency("USD");
        paymentDetails.setFailureReason("Insufficient funds");
        paymentDetails.setPaymentMethod("Credit Card");
        paymentDetails.setPaymentStatus("FAILED");
        paymentDetails.setTransactionDate(LocalDateTime.now());

        mockMvc.perform(post("/api/notifications/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetails)))
                .andExpect(status().isOk());
    }
}
