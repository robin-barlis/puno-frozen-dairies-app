package com.example.application.data.entity.orders;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
import com.example.application.data.entity.payment.Payment;


@Entity
@Table(name = "pfdi_orders")
public class Order implements Serializable {

	private static final long serialVersionUID = 8250019492935742153L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
	@Fetch(FetchMode.SELECT)
	private Customer customer;
	
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

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "order_id")
	private Set<OrderItems> orderItems;
	
	@OneToMany(mappedBy = "orderId", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SELECT)
	private List<Payment> payments;
	
	private LocalDateTime creationDate;
	
	private LocalDateTime updatedDate;
	
	private LocalDateTime checkedDate;
	
	private LocalDateTime deliveryDate;
	
	private Integer invoiceId;
	
	private BigDecimal amountSrp;

	private Integer deliveryReceiptId;
	
	private Integer stockTransferId;
	
	private LocalDate dueDate;
	
	private BigDecimal amountDue;
	
	private BigDecimal balance;

	private Integer stockOrderNumber;
	
	private String status;
	
	private BigDecimal discount;
	
	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public BigDecimal getAmountDue() {
		return amountDue;
	}

	public void setAmountDue(BigDecimal amountDue) {
		this.amountDue = amountDue;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	
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
	
	public List<Payment> getPayments() {
		return payments;
	}

	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}


	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public BigDecimal getAmountSrp() {
		return amountSrp;
	}

	public void setAmountSrp(BigDecimal amountSrp) {
		this.amountSrp = amountSrp;
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

	public LocalDateTime getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(LocalDateTime deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public BigDecimal getDiscount() {
		return discount;
	}

	public void setDiscount(BigDecimal discount) {
		this.discount = discount;
	}

}
