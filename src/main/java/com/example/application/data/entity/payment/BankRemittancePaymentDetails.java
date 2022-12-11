package com.example.application.data.entity.payment;

import java.io.Serializable;
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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.orders.Order;


@Entity
@Table(name = "pfdi_payments_bank_remittance_details")
public class BankRemittancePaymentDetails implements Serializable {

	private static final long serialVersionUID = 8250019492935742153L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
	@Fetch(FetchMode.SELECT)
	private Payment payment;
	
	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
	@Fetch(FetchMode.SELECT)
	private AppUser createdBy;
	
	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by", nullable = false)
	@Fetch(FetchMode.SELECT)
	private AppUser updatedBy;
	

	private LocalDateTime createdDate;
	private LocalDateTime updatedDate;
	
	private LocalDate depositDate;
	private LocalDate salesDateCovered;
	private String accountName;
	private String accountNumber;
	
	@OneToOne()
    @JoinColumn(name = "bankId", nullable = false)
	@Fetch(FetchMode.SELECT)
	private Banks bankId;
	private String referenceNumber;
	

	public Banks getBankId() {
		return bankId;
	}


	public void setBankId(Banks bankId) {
		this.bankId = bankId;
	}


	public Payment getPayment() {
		return payment;
	}


	public LocalDate getDepositDate() {
		return depositDate;
	}


	public void setDepositDate(LocalDate depositDate) {
		this.depositDate = depositDate;
	}


	public LocalDate getSalesDateCovered() {
		return salesDateCovered;
	}


	public void setSalesDateCovered(LocalDate salesDateCovered) {
		this.salesDateCovered = salesDateCovered;
	}


	public String getReferenceNumber() {
		return referenceNumber;
	}


	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}


	public void setPayment(Payment payment) {
		this.payment = payment;
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


	public String getAccountNumber() {
		return accountNumber;
	}


	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}


	public String getAccountName() {
		return accountName;
	}


	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}



}
