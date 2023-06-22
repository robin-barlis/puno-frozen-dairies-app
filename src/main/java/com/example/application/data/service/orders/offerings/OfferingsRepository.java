package com.example.application.data.service.orders.offerings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.application.data.entity.orders.offerings.Offerings;
@Repository
public interface OfferingsRepository extends JpaRepository<Offerings, Integer> {
	
	

}
