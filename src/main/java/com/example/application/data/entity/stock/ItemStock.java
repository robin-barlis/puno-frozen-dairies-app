package com.example.application.data.entity.stock;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.Size;


@Entity
@Table(name = "pfdi_item_inventory")
public class ItemStock implements Serializable {

	private static final long serialVersionUID = 8250019492935742153L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private Integer availableStock;
	private Integer reservedStock;
	
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
	private Product product;
	
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "size_id", nullable = false)
	private Size size;

	
	private String updatedBy;
	
	public Integer getId() {
		return id;
	}


	public Product getProduct() {
		return product;
	}


	public void setProduct(Product product) {
		this.product = product;
	}


	public Size getSize() {
		return size;
	}


	public void setSize(Size size) {
		this.size = size;
	}


	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAvailableStock() {
		return availableStock;
	}

	public void setAvailableStock(Integer availableStock) {
		this.availableStock = availableStock;
	}

	public Integer getReservedStock() {
		return reservedStock;
	}

	public void setReservedStock(Integer reservedStock) {
		this.reservedStock = reservedStock;
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
		ItemStock other = (ItemStock) obj;
		if (getId() == null || other.getId() == null) {
			return false;
		}
		return getId().equals(other.getId());
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

}
