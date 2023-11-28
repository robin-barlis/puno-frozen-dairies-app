package com.example.application.data.service.payment;

import java.util.List;
import java.util.Map;

import com.example.application.data.entity.payment.Payment;

public interface PaymentRepositoryCustom {
	
	List<Payment> filterByDates(Map<String, Object> criteriaMap);
	
	Map<String, List<Payment>> getPaymentsMapByFilterDates(Map<String, Object> criteriaMap);
	
	List<Payment> filterBy(Map<String, Object> criteriaMap);


}
