package com.example.application.data.entity.stock;

import java.util.List;

public class Inventory {
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String category;
	
	private String sizeName;
	
	private Integer quantity;

	public Inventory(String name, String category, String sizeName, Integer quantity, List<Inventory> subinventory) {
		super();
		this.name = name;
		this.category = category;
		this.sizeName = sizeName;
		this.quantity = quantity;
		this.subinventory = subinventory;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public String getSizeName() {
		return sizeName;
	}

	public Integer getQuantity() {
		return quantity;
	}
	
	public List<Inventory> getSubinventory() {
		return subinventory;
	}

	private List<Inventory> subinventory;

}
