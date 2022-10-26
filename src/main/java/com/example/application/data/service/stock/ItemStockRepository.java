package com.example.application.data.service.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.application.data.entity.products.Size;
import com.example.application.data.entity.stock.ItemStock;
@Repository
public interface ItemStockRepository extends JpaRepository<ItemStock, Integer> {

}
