package com.example.application.data.service.orders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;
import com.google.gwt.thirdparty.guava.common.collect.Lists;

public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Order> filterBy(Map<String, Object> criteriaMap) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Order> query = cb.createQuery(Order.class);
	
		Root<Order> order = query.from(Order.class);
		List<Predicate> predicates = Lists.newArrayList();
		
		
		for (Entry<String, Object> entry : criteriaMap.entrySet()) {

			if ("store".equals(entry.getKey())) {
				
				Object object = entry.getValue();
				@SuppressWarnings("unchecked")
				Set<Customer> customers = (Set<Customer>) object;
				List<String> storeNames = customers.stream().map(e-> e.getStoreName()).collect(Collectors.toList());
				Expression<Object> storeExpression = order.get("customer").get("storeName");
				Predicate storePredicate = storeExpression.in(storeNames);
				predicates.add(storePredicate);

			} else if ("orderStatus".equals(entry.getKey())) {
				
				Object object = entry.getValue();
				@SuppressWarnings("unchecked")
				List<String> orderStatus = (List<String>) object;
				
				
				Expression<Object> storeExpression = order.get("status");
				Predicate storePredicate = storeExpression.in(orderStatus);
				predicates.add(storePredicate);
			} else if ("ordersDate".equals(entry.getKey())) {
				
				@SuppressWarnings("unchecked")
				Map<String, LocalDate> orderDates = (Map<String, LocalDate>) entry.getValue();
	
				
				Predicate predicate = cb.between(order.get("creationDate"), orderDates.get("orderDateFrom").atStartOfDay(), orderDates.get("orderDateTo").atTime(LocalTime.MAX));
				predicates.add(predicate);
			} else if ("dueDates".equals(entry.getKey())) {
				
				@SuppressWarnings("unchecked")
				Map<String, LocalDate> orderDates = (Map<String, LocalDate>) entry.getValue();
	
				
				Predicate predicate = cb.between(order.get("dueDate"), orderDates.get("dueDateFrom").atStartOfDay(), orderDates.get("dueDateTo").atTime(LocalTime.MAX));
				predicates.add(predicate);
			}  else {

				Predicate ownerPredicate = cb.equal(order.get(entry.getKey()), entry.getValue());
				predicates.add(ownerPredicate);
			}
		}

		query.where(predicates.toArray(new Predicate[]{}));
		query.orderBy(cb.desc(order.get("creationDate")));

		return entityManager.createQuery(query).getResultList();
	}

}
