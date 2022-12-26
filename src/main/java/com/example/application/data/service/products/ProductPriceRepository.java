package com.example.application.data.service.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.ProductPrice;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, Integer> {
	
    @Query(value = "DELETE FROM ProductPrice pp WHERE pp.product = :product", nativeQuery = true)
    void deleteByProduct(@Param("product") Product productId);

}
