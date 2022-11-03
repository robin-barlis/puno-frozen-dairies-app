package com.example.application.data.entity.products;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "pfdi_product_price")
public class ProductPrice implements Serializable {

	private static final long serialVersionUID = 8250019492935742153L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
		
	private Integer sizeId;
	
	private Integer customerTagId;
	
	private Integer locationTagId;
	
	private Integer categoryId;
		
	private BigDecimal transferPrice;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
	private Product product;
	
	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Integer getSizeId() {
		return sizeId;
	}

	public void setSizeId(Integer sizeId) {
		this.sizeId = sizeId;
	}

	public Integer getCustomerTagId() {
		return customerTagId;
	}

	public void setCustomerTagId(Integer customerTagId) {
		this.customerTagId = customerTagId;
	}

	public Integer getLocationTagId() {
		return locationTagId;
	}

	public void setLocationTagId(Integer locationTagId) {
		this.locationTagId = locationTagId;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public BigDecimal getTransferPrice() {
		return transferPrice;
	}

	public void setTransferPrice(BigDecimal transferPrice) {
		this.transferPrice = transferPrice;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		if (getId() != null) {
			return getId().hashCode();
		}
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ProductPrice other = (ProductPrice) obj;
		if (getId() == null || other.getId() == null) {
			return false;
		}
		return getId().equals(other.getId());
	}

}
