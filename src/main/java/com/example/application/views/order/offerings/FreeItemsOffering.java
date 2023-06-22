package com.example.application.views.order.offerings;

import java.util.Set;

import com.example.application.data.DiscountType;
import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.Size;

public class FreeItemsOffering extends AbstractOffering {
	
	private final Product product;
	private final Set<Size> sizes;
	private final Integer quantity;
	public FreeItemsOffering(DiscountType discountType, Product product, Set<Size> sizes, Integer quantity) {
		super(discountType);
		this.product = product;
		this.sizes = sizes;
		this.quantity = quantity;
	}
	public Product getProduct() {
		return product;
	}
	public Set<Size> getSizes() {
		return sizes;
	}
	public Integer getQuantity() {
		return quantity;
	}

	
}
