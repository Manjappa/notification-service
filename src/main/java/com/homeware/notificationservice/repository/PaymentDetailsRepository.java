package com.homeware.notificationservice.repository;

import com.homeware.notificationservice.entity.PaymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentDetailsRepository extends JpaRepository<PaymentDetails, Long> {
    
    Optional<PaymentDetails> findByTransactionId(String transactionId);
    
    List<PaymentDetails> findByMerchantEmail(String merchantEmail);
    
    List<PaymentDetails> findByPaymentStatus(String paymentStatus);
    
    List<PaymentDetails> findByMerchantEmailAndPaymentStatus(String merchantEmail, String paymentStatus);
}

