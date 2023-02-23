package com.example.application.data.entity.customers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

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

import com.example.application.data.entity.products.CustomerTag;
import com.example.application.data.entity.products.LocationTag;

@Entity
@Table(name = "pfdi_customers")
public class Customer implements Serializable {

	private static final long serialVersionUID = 8250019492935742153L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String storeName;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "customer_tag_id")
	@Fetch(FetchMode.SELECT)
	private CustomerTag customerTagId;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "location_tag_id")
	@Fetch(FetchMode.SELECT)
	private LocationTag locationTagId;
	
	private String ownerName;
	private String tinNumber;
	private BigDecimal contactNumber;
	private String address;
	private LocalDate contractStartDate;
	private LocalDate contractEndDate;
	
	
	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public LocationTag getLocationTagId() {
		return locationTagId;
	}

	public void setLocationTagId(LocationTag locationTagId) {
		this.locationTagId = locationTagId;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public BigDecimal getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(BigDecimal contactNumber) {
		this.contactNumber = contactNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public LocalDate getContractStartDate() {
		return contractStartDate;
	}

	public void setContractStartDate(LocalDate contractStartDate) {
		this.contractStartDate = contractStartDate;
	}

	public LocalDate getContractEndDate() {
		return contractEndDate;
	}

	public void setContractEndDate(LocalDate contractEndDate) {
		this.contractEndDate = contractEndDate;
	}

	public CustomerTag getCustomerTagId() {
		return customerTagId;
	}

	public void setCustomerTagId(CustomerTag customerTagId) {
		this.customerTagId = customerTagId;
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
		Customer other = (Customer) obj;
		if (getId() == null || other.getId() == null) {
			return false;
		}
		return getId().equals(other.getId());
	}

	public String getTinNumber() {
		return tinNumber;
	}

	public void setTinNumber(String tinNumber) {
		this.tinNumber = tinNumber;
	}

}
