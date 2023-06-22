package com.example.application.views.order.offerings;

import java.util.Set;

import com.example.application.data.DiscountType;
import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.Size;

public class BuyGetOffering extends AbstractOffering {
	
	private final Product product;
	private final Set<Size> sizes;
	private final Integer getQuantity;
	private final Integer buyQuantity;

	public BuyGetOffering(DiscountType discountType, Product product, Set<Size> sizes, Integer getQuantity, Integer buyQuantity) {
		super(discountType);
		this.product = product;
		this.getQuantity=getQuantity;
		this.sizes = sizes;
		this.buyQuantity = buyQuantity;
	}

	public Integer getBuyQuantity() {
		return buyQuantity;
	}

	public Product getProduct() {
		return product;
	}

	public Set<Size> getSizes() {
		return sizes;
	}

	public Integer getGetQuantity() {
		return getQuantity;
	}

}
