package com.example.application.data.entity.orders;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.stock.ItemStock;


@Entity
@Table(name = "pfdi_order_items")
public class OrderItems implements Serializable {

	private static final long serialVersionUID = 8250019492935742153L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id", nullable = false)
	private Order order;
	
	private Integer quantity;

	private BigDecimal productPrice;
	
	private BigDecimal productSrp;
	
	private LocalDateTime createdDate;


	private LocalDateTime updatedDate;
	
	@OneToOne(fetch = FetchType.EAGER, cascade={CascadeType.MERGE})
    @JoinColumn(name = "item_inventory_id", nullable = false)
	private ItemStock itemInventory;
	
	@OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "updated_by", nullable = false)
	private AppUser updatedBy;
		
	@OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
	private AppUser createdBy;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(BigDecimal productPrice) {
		this.productPrice = productPrice;
	}

	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public LocalDateTime getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(LocalDateTime updatedDate) {
		this.updatedDate = updatedDate;
	}

	public ItemStock getItemInventory() {
		return itemInventory;
	}

	public void setItemInventory(ItemStock itemInventory) {
		this.itemInventory = itemInventory;
	}

	public AppUser getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(AppUser updatedBy) {
		this.updatedBy = updatedBy;
	}

	public AppUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(AppUser createdBy) {
		this.createdBy = createdBy;
	}
	


	public BigDecimal getProductSrp() {
		return productSrp;
	}

	public void setProductSrp(BigDecimal productSrp) {
		this.productSrp = productSrp;
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
		OrderItems other = (OrderItems) obj;
		if (getId() == null || other.getId() == null) {
			return false;
		}
		return getId().equals(other.getId());
	}

}
