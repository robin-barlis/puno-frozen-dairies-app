package com.example.application.data.entity.products;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.example.application.data.entity.stock.ItemStock;

@Entity
@Table(name = "pfdi_products")
public class Product implements Serializable {

	private static final long serialVersionUID = 8250019492935742153L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String productName;

	private String productShortCode;

	private String productDescription;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "category_id")
	@Fetch(FetchMode.SELECT)
	private Category category;
	
	private String productPictureUrl;
	
	
	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id")
	private Set<ProductPrice> productPrices;
	
	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id")
	private Set<ItemStock> itemStock;

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Set<ProductPrice> getProductPrices() {
		return productPrices;
	}

	public void setProductPrices(Set<ProductPrice> productPrices) {
		this.productPrices = productPrices;
	}

	public String getProductDescription() {
		return productDescription;
	}

	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
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

	public String getProductPictureUrl() {
		return productPictureUrl;
	}

	public void setProductPictureUrl(String productPictureUrl) {
		this.productPictureUrl = productPictureUrl;
	}

	public String getProductShortCode() {
		return productShortCode;
	}

	public void setProductShortCode(String productShortCode) {
		this.productShortCode = productShortCode;
	}
	

	
	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Set<ItemStock> getItemStock() {
		return itemStock;
	}

	public void setItemStock(Set<ItemStock> itemStock) {
		this.itemStock = itemStock;
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
		Product other = (Product) obj;
		if (getId() == null || other.getId() == null) {
			return false;
		}
		return getId().equals(other.getId());
	}


}
