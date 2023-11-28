package com.example.application.data.service.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.application.data.entity.payment.Payment;
@Repository
public interface PaymentsRepository extends JpaRepository<Payment, Integer>, PaymentRepositoryCustom {
	

}
