package com.example.application.data.entity.stock;

import java.math.BigDecimal;

import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.Size;
import com.vaadin.flow.component.textfield.NumberField;

public class Items {
	
	private NumberField numberField;
	private BigDecimal price;
	private Size size;
	private Product product;
	private ItemStock itemStock;
	
	public Items(NumberField numberField, BigDecimal price, Size size, Product product, ItemStock itemStock) {
		super();
		this.numberField = numberField;
		this.price = price;
		this.size = size;
		this.product = product;
		this.itemStock = itemStock;
	}

	public NumberField getNumberField() {
		return numberField;
	}

	public void setNumberField(NumberField numberField) {
		this.numberField = numberField;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Size getSize() {
		return size;
	}

	public void setSize(Size size) {
		this.size = size;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public ItemStock getItemStock() {
		return itemStock;
	}

	public void setItemStock(ItemStock itemStock) {
		this.itemStock = itemStock;
	}
	

	
}
