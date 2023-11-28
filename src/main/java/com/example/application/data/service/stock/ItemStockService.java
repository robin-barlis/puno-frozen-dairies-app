package com.example.application.data.service.stock;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.stock.Inventory;
import com.example.application.data.entity.stock.ItemStock;
import com.example.application.utils.PfdiUtil;
import com.google.common.collect.Lists;


@Service
public class ItemStockService {
	
	private final ItemStockRepository repository;

	@Autowired
	public ItemStockService(ItemStockRepository repository) {
		this.repository = repository;
	}

	
	public Optional<ItemStock> get(Integer id) {
		return repository.findById(id);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<ItemStock> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<ItemStock> listAll(Sort sort) {
		return repository.findAll(sort);
	}
	
	public List<Inventory> listAllByProduct(Sort sort) {
		
		
		Sort sortByCategory = Sort.by("product_category_categoryName").ascending();
		
		List<ItemStock> itemStocks = repository.findAll(sortByCategory);
		
		if (itemStocks != null) {
			Map<String, List<ItemStock>> inventoryPerProduct = itemStocks.stream()
					.collect(Collectors.groupingBy(stock -> stock.getProduct().getProductName()));
			
			List<Inventory> inventoriesProductName = Lists.newArrayList();
			
			for (Entry<String, List<ItemStock>> inventory : inventoryPerProduct.entrySet()) {
				
				
				
				List<ItemStock> stocks = inventory.getValue();
				List<Inventory> subInventories = stocks.stream().map(e -> {
						return new Inventory(e.getSize().getSizeName(), e.getProduct().getCategory().getCategoryName(),
								"", e.getAvailableStock(), Lists.newArrayList());
				}).collect(Collectors.toList());
				subInventories.sort(PfdiUtil.itemStockComparator);
				
				if (!subInventories.isEmpty()) {
					String firstValue = subInventories.get(0).getCategory();
					int allCount = subInventories.stream().mapToInt(i -> i.getQuantity().intValue()).sum();
					Inventory inventoryKey = new Inventory(inventory.getKey(), firstValue, "", allCount, subInventories);
					inventoriesProductName.add(inventoryKey);

				}

				
			
				
			}
			
			return inventoriesProductName;
		}
		
	
		
		return null;
	}

	public ItemStock update(Object entity) {
		return repository.save((ItemStock)entity);
		
	}
	
	public List<ItemStock> updateAll(Collection<ItemStock> entities) {
		return repository.saveAll(entities);
		
	}


}
