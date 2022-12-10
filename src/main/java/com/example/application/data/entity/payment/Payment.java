package com.example.application.data.entity.payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;


@Entity
@Table(name = "pfdi_payments")
public class Payment implements Serializable {

	private static final long serialVersionUID = 8250019492935742153L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
	@Fetch(FetchMode.SELECT)
	@NotEmpty(message = "Order must be selected")
	private Order orderId;
	
	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
	@Fetch(FetchMode.SELECT)
	@NotEmpty(message = "Customer must be selected")
	private Customer customer;
	
	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
	@Fetch(FetchMode.SELECT)
	private AppUser createdBy;
	
	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by", nullable = false)
	@Fetch(FetchMode.SELECT)
	private AppUser updatedBy;
	
	@OneToOne(mappedBy = "payment", cascade = CascadeType.ALL)
	private BankRemittancePaymentDetails bankRemittanceDetails;
	

	@OneToOne(mappedBy = "payment", cascade = CascadeType.ALL)
	private ChequePaymentDetails chequePaymentDetails;
	

	@OneToOne(mappedBy = "payment", cascade = CascadeType.ALL)
	private CashPaymentDetails cashPaymentDetails;
	
	private Integer paymentNumber;
	
	@NotEmpty(message = "Amount must have a value")
	private BigDecimal amount;
	
	private LocalDate paymentDate;
	
	private String paymentMode;
	
	private String note;
	
	private String status;
	
	private BigDecimal balance;

	private LocalDate dueDate;
	
	private LocalDateTime createdDate;
	
	private LocalDateTime updatedDate;


	public Order getOrderId() {
		return orderId;
	}


	public void setOrderId(Order orderId) {
		this.orderId = orderId;
	}


	public Integer getPaymentNumber() {
		return paymentNumber;
	}


	public void setPaymentNumber(Integer paymentNumber) {
		this.paymentNumber = paymentNumber;
	}


	public BigDecimal getAmount() {
		return amount;
	}


	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}


	public LocalDate getPaymentDate() {
		return paymentDate;
	}


	public void setPaymentDate(LocalDate paymentDate) {
		this.paymentDate = paymentDate;
	}


	public String getPaymentMode() {
		return paymentMode;
	}


	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}


	public String getNote() {
		return note;
	}


	public void setNote(String note) {
		this.note = note;
	}


	public LocalDate getDueDate() {
		return dueDate;
	}


	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}


	public Customer getCustomer() {
		return customer;
	}


	public void setCustomer(Customer customer) {
		this.customer = customer;
	}


	public AppUser getCreatedBy() {
		return createdBy;
	}


	public void setCreatedBy(AppUser createdBy) {
		this.createdBy = createdBy;
	}


	public AppUser getUpdatedBy() {
		return updatedBy;
	}


	public void setUpdatedBy(AppUser updatedBy) {
		this.updatedBy = updatedBy;
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


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public BankRemittancePaymentDetails getBankRemittanceDetails() {
		return bankRemittanceDetails;
	}


	public void setBankRemittanceDetails(BankRemittancePaymentDetails bankRemittanceDetails) {
		this.bankRemittanceDetails = bankRemittanceDetails;
	}


	public ChequePaymentDetails getChequePaymentDetails() {
		return chequePaymentDetails;
	}


	public void setChequePaymentDetails(ChequePaymentDetails chequePaymentDetails) {
		this.chequePaymentDetails = chequePaymentDetails;
	}


	public CashPaymentDetails getCashPaymentDetails() {
		return cashPaymentDetails;
	}


	public void setCashPaymentDetails(CashPaymentDetails cashPaymentDetails) {
		this.cashPaymentDetails = cashPaymentDetails;
	}


	public BigDecimal getBalance() {
		return balance;
	}


	public void setBalance(BigDecimal balance) {
		this.balance = balance;
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



}
