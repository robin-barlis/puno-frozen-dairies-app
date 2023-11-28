package com.example.application.data.service.payment;

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
import com.example.application.data.entity.payment.Payment;
import com.google.gwt.thirdparty.guava.common.collect.Lists;


public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {
	
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Payment> filterByDates(Map<String, Object> criteriaMap) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Payment> query = cb.createQuery(Payment.class);
		Root<Payment> orderQuery = query.from(Payment.class);
		List<Predicate> predicates = Lists.newArrayList();
		
		for (Entry<String, Object> entry : criteriaMap.entrySet()) {

			if ("paymentDateCriteria".equals(entry.getKey())) {
				
				@SuppressWarnings("unchecked")
				Map<String, LocalDate> orderDates = (Map<String, LocalDate>) entry.getValue();
				Predicate predicate = cb.between(orderQuery.get("paymentDate"), 
						orderDates.get("paymentDateFrom"), orderDates.get("paymentDateTo"));
				predicates.add(predicate);
			}  
		}

		query.where(predicates.toArray(new Predicate[]{}));


		return entityManager.createQuery(query).getResultList();
	}

	@Override
	public Map<String, List<Payment>> getPaymentsMapByFilterDates(Map<String, Object> criteriaMap) {
		List<Payment> payments = filterByDates(criteriaMap);
		
		
		Map<String, List<Payment>> paymentByType = payments.stream().collect(Collectors.groupingBy(Payment::getPaymentMode));
		
		
		return paymentByType;
	}

	@Override
	public List<Payment> filterBy(Map<String, Object> criteriaMap) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Payment> query = cb.createQuery(Payment.class);
		Root<Payment> payment = query.from(Payment.class);
		List<Predicate> predicates = Lists.newArrayList();
		
		for (Entry<String, Object> entry : criteriaMap.entrySet()) {

			if ("store".equals(entry.getKey())) {
				
				Object object = entry.getValue();
				@SuppressWarnings("unchecked")
				Set<Customer> customers = (Set<Customer>) object;
				List<String> storeNames = customers.stream().map(e-> e.getStoreName()).collect(Collectors.toList());
				Expression<Object> storeExpression = payment.get("customer").get("storeName");
				Predicate storePredicate = storeExpression.in(storeNames);
				predicates.add(storePredicate);

			} else if ("paymentMode".equals(entry.getKey())) {
				
				Object object = entry.getValue();
				@SuppressWarnings("unchecked")
				List<String> orderStatus = (List<String>) object;
				
				
				Expression<Object> storeExpression = payment.get("paymentMode");
				Predicate storePredicate = storeExpression.in(orderStatus);
				predicates.add(storePredicate);
			} else if ("paymentDate".equals(entry.getKey())) {
				
				@SuppressWarnings("unchecked")
				Map<String, LocalDate> paymentDates = (Map<String, LocalDate>) entry.getValue();
	
				
				Predicate predicate = cb.between(payment.get("createdDate"), paymentDates.get("paymentDateFrom").atStartOfDay(), paymentDates.get("paymentDateTo").atTime(LocalTime.MAX));
				predicates.add(predicate);
			} else if ("stockOrderNumber".equals(entry.getKey())) {
				
				Object object = entry.getValue();
				
				
				Expression<Object> storeExpression = payment.get("orderId").get("stockOrderNumber");
				Predicate storePredicate = storeExpression.in(object);
				predicates.add(storePredicate);

			} else {

				Predicate predicate = cb.equal(payment.get(entry.getKey()), entry.getValue());
				predicates.add(predicate);
			}
		}

		query.where(predicates.toArray(new Predicate[]{}));
		query.orderBy(cb.asc(payment.get("createdDate")));

		return entityManager.createQuery(query).getResultList();
	}


}
