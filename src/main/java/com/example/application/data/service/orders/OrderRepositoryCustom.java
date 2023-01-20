package com.example.application.data.service.orders;

import java.util.List;
import java.util.Map;

import com.example.application.data.entity.orders.Order;

public interface OrderRepositoryCustom {
	
	List<Order> filterBy(Map<String, Object> criteriaMap);

}
