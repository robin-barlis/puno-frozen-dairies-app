package com.example.application.data.service.products;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Product;
import com.google.gwt.thirdparty.guava.common.collect.Lists;

public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {
	
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Product> filterByCategory(Category category) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Product> query = cb.createQuery(Product.class);
		Root<Product> order = query.from(Product.class);
		List<Predicate> predicates = Lists.newArrayList();
		
		Expression<Object> storeExpression = order.get("category").get("categoryName");
		Predicate storePredicate = storeExpression.in(category.getCategoryName());
		predicates.add(storePredicate);
		
		query.where(predicates.toArray(new Predicate[]{}));
		query.orderBy(cb.asc(order.get("sortingIndex")));
		return entityManager.createQuery(query).getResultList();
	}

}
