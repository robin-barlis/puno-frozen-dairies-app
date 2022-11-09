package com.example.application.data.service.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.application.data.entity.orders.Order;
@Repository
public interface OrdersRepository extends JpaRepository<Order, Integer> {
	
    @Query(value = "SELECT max(stockOrderNumber) FROM Order")
    Integer findMaxSONumber();

}
