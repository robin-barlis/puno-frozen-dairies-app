package com.example.application.data.entity.orders.offerings;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
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
import org.hibernate.annotations.Type;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.OrderItems;
import com.example.application.data.entity.payment.Payment;


@Entity
@Table(name = "pfdi_discounts")
public class Offerings implements Serializable {

	private static final long serialVersionUID = 8250019492935742153L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String discountName;
	private String discountDescription;
	private String discountType;
	
	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
	@Fetch(FetchMode.SELECT)
	private AppUser createdBy;
	
	private LocalDate createDate;
	private LocalDate effectiveDateFrom;
	private LocalDate effectiveDateTo;
	private Boolean isActive;
	
	private String discountCondition;
	private String discountResults;
	

	
	public Integer getId() {
		return id;
	}



	public void setId(Integer id) {
		this.id = id;
	}



	public String getDiscountName() {
		return discountName;
	}



	public void setDiscountName(String discountName) {
		this.discountName = discountName;
	}



	public String getDiscountDescription() {
		return discountDescription;
	}



	public void setDiscountDescription(String discountDescription) {
		this.discountDescription = discountDescription;
	}



	public String getDiscountType() {
		return discountType;
	}



	public void setDiscountType(String discountType) {
		this.discountType = discountType;
	}



	public AppUser getCreatedBy() {
		return createdBy;
	}



	public void setCreatedBy(AppUser createdBy) {
		this.createdBy = createdBy;
	}



	public LocalDate getCreateDate() {
		return createDate;
	}



	public void setCreateDate(LocalDate createDate) {
		this.createDate = createDate;
	}



	public LocalDate getEffectiveDateFrom() {
		return effectiveDateFrom;
	}



	public void setEffectiveDateFrom(LocalDate effectiveDateFrom) {
		this.effectiveDateFrom = effectiveDateFrom;
	}



	public LocalDate getEffectiveDateTo() {
		return effectiveDateTo;
	}



	public void setEffectiveDateTo(LocalDate effectiveDateTo) {
		this.effectiveDateTo = effectiveDateTo;
	}



	public Boolean getIsActive() {
		return isActive;
	}



	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}



	public String getDiscountCondition() {
		return discountCondition;
	}



	public void setDiscountCondition(String discountCondition) {
		this.discountCondition = discountCondition;
	}



	public String getDiscountResults() {
		return discountResults;
	}



	public void setDiscountResults(String discountResults) {
		this.discountResults = discountResults;
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
		Offerings other = (Offerings) obj;
		if (getId() == null || other.getId() == null) {
			return false;
		}
		return getId().equals(other.getId());
	}


}
