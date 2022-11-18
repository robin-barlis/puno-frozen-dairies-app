package com.example.application.data.service.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.application.data.entity.orders.DocumentTrackingNumber;
@Repository
public interface DocumentTrackingNumberRepository extends JpaRepository<DocumentTrackingNumber, Integer> {
	
    @Query(value = "SELECT number FROM DocumentTrackingNumber where type = 'INVOICE_NUMBER'")
    Integer findInvoiceNumber();
    
    @Query(value = "SELECT number FROM DocumentTrackingNumber where type = 'DELIVERY_RECEIPT_NUMBER'")
    Integer findDeliveryReceiptNumber();
    
    @Query(value = "SELECT number FROM DocumentTrackingNumber where type = 'STOCK_TRANSFER_NUMBER'")
    Integer findSTNumber();
    
    @Query(value = "SELECT number FROM DocumentTrackingNumber where type = 'PAYMENT_NUMBER'")
    Integer findPaymentNumber();
    
    
    @Query(value = "SELECT dtn FROM DocumentTrackingNumber dtn where dtn.type = ?1")
    DocumentTrackingNumber findByType(String type);

}
