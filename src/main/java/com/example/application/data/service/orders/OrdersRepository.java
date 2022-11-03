package com.example.application.data.service.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.application.data.entity.orders.Order;
@Repository
public interface OrdersRepository extends JpaRepository<Order, Integer> {

}
