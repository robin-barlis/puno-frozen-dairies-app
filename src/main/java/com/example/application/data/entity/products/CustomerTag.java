package com.example.application.data.entity.products;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "pfdi_customer_tag")
public class CustomerTag implements Serializable {

	private static final long serialVersionUID = 8250019492935742153L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String customerTagName;

	private String customerTagDescription;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "pfdi_customer_location_tag_mapping", 
			joinColumns = {@JoinColumn(name = "cutomer_tag_id") },
			inverseJoinColumns = { @JoinColumn(name = "location_tag_id") })
	@Fetch(FetchMode.SELECT)
	private Set<LocationTag> locationTagSet;

	public Set<LocationTag> getLocationTagSet() {
		return locationTagSet;
	}

	public void setLocationTagSet(Set<LocationTag> locationTagSet) {
		this.locationTagSet = locationTagSet;
	}

	@Override
	public int hashCode() {
		if (getId() != null) {
			return getId().hashCode();
		}
		return super.hashCode();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCustomerTagName() {
		return customerTagName;
	}

	public void setCustomerTagName(String customerTagName) {
		this.customerTagName = customerTagName;
	}

	public String getCustomerTagDescription() {
		return customerTagDescription;
	}

	public void setCustomerTagDescription(String customerTagDescription) {
		this.customerTagDescription = customerTagDescription;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
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
		CustomerTag other = (CustomerTag) obj;
		if (getId() == null || other.getId() == null) {
			return false;
		}
		return getId().equals(other.getId());
	}

}
