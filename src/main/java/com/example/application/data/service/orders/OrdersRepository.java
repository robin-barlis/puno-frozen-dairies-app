package com.example.application.data.service.orders;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.application.data.entity.orders.Order;
@Repository
public interface OrdersRepository extends JpaRepository<Order, Integer>, OrderRepositoryCustom {
	
    @Query(value = "SELECT max(stockOrderNumber) FROM Order")
    Integer findMaxSONumber();
    
    @Query(value = "SELECT order FROM Order order where order.customer.storeName = :storeName")
    List<Order> findReadyForPaymentOrdersByCustomerName(@Param("storeName") String storeName);
    
    @Query(value = "SELECT order FROM Order order where order.balance > 0 order by order.stockOrderNumber asc")
    List<Order> findReadyForPaymentOrders();
	

}
