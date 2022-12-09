package com.example.application.data.entity.payment;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.orders.Order;


@Entity
@Table(name = "pfdi_payments_cash_details")
public class ChequePaymentDetails implements Serializable {

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
	private String chequeNumber;
	private String bankName;
	private LocalDate chequeIssueDate;
	

	public String getChequeNumber() {
		return chequeNumber;
	}


	public void setChequeNumber(String chequeNumber) {
		this.chequeNumber = chequeNumber;
	}


	public String getBankName() {
		return bankName;
	}


	public void setBankName(String bankName) {
		this.bankName = bankName;
	}


	public LocalDate getChequeIssueDate() {
		return chequeIssueDate;
	}


	public void setChequeIssueDate(LocalDate chequeIssueDate) {
		this.chequeIssueDate = chequeIssueDate;
	}


	public Payment getPayment() {
		return payment;
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



}
