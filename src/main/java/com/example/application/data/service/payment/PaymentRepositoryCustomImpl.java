package com.example.application.data.service.payment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.example.application.data.entity.payment.Payment;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.flow.spring.annotation.SpringComponent;


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

}
