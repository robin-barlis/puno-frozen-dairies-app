package com.example.application.data.entity.orders;

import java.io.Serializable;
import java.time.LocalDateTime;
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

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.customers.Customer;


@Entity
@Table(name = "pfdi_orders")
public class Order implements Serializable {

	private static final long serialVersionUID = 8250019492935742153L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private Integer stockOrderNumber;
	
	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
	@Fetch(FetchMode.SELECT)
	private Customer customer;
	
	private String status;
	
	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
	@Fetch(FetchMode.SELECT)
	private AppUser createdByUser;
	
	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by_user_id", nullable = false)
	@Fetch(FetchMode.SELECT)
	private AppUser updatedByUserId;
	
	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "checked_by", nullable = true)
	@Fetch(FetchMode.SELECT)
	private AppUser checkedByUser;

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinColumn(name = "order_id")
	private Set<OrderItems> orderItems;
	
	private LocalDateTime creationDate;
	
	private LocalDateTime updatedDate;
	
	private LocalDateTime checkedDate;
	
	private Integer invoiceId;
	
	private Integer deliveryReceiptId;
	
	private Integer stockTransferId;
	
	public Integer getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(Integer invoiceId) {
		this.invoiceId = invoiceId;
	}

	public Integer getDeliveryReceiptId() {
		return deliveryReceiptId;
	}

	public void setDeliveryReceiptId(Integer deliveryReceiptId) {
		this.deliveryReceiptId = deliveryReceiptId;
	}

	public Integer getStockTransferId() {
		return stockTransferId;
	}

	public void setStockTransferId(Integer stockTransferId) {
		this.stockTransferId = stockTransferId;
	}

	private String notes;
	
	
	public AppUser getUpdatedByUserId() {
		return updatedByUserId;
	}

	public void setUpdatedByUserId(AppUser updatedByUserId) {
		this.updatedByUserId = updatedByUserId;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public LocalDateTime getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(LocalDateTime updatedDate) {
		this.updatedDate = updatedDate;
	}

	public LocalDateTime getCheckedDate() {
		return checkedDate;
	}

	public void setCheckedDate(LocalDateTime checkedDate) {
		this.checkedDate = checkedDate;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Integer getStockOrderNumber() {
		return stockOrderNumber;
	}

	public void setStockOrderNumber(Integer stockOrderNumber) {
		this.stockOrderNumber = stockOrderNumber;
	}

	public Customer getCustomer() {
		return customer;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public AppUser getCreatedByUser() {
		return createdByUser;
	}

	public void setCreatedByUser(AppUser createdBy) {
		this.createdByUser = createdBy;
	}

	public AppUser getUpdatedByUser() {
		return updatedByUserId;
	}

	public void setUpdatedByUser(AppUser updatedByUserId) {
		this.updatedByUserId = updatedByUserId;
	}

	public AppUser getCheckedByUser() {
		return checkedByUser;
	}

	public void setCheckedByUser(AppUser checkedBy) {
		this.checkedByUser = checkedBy;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	
	public Set<OrderItems> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(Set<OrderItems> orderItems) {
		this.orderItems = orderItems;
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
		Order other = (Order) obj;
		if (getId() == null || other.getId() == null) {
			return false;
		}
		return getId().equals(other.getId());
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}


}
