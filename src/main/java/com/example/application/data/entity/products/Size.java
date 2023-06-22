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
@Table(name = "pfdi_sizes")
public class Size implements Serializable {

	private static final long serialVersionUID = 8250019492935742153L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String sizeName;


	private String sizeDescription;
	
	private String sizeCategory;
	
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "pfdi_size_customer_tag_mapping", 
			joinColumns = {@JoinColumn(name = "size_id") },
			inverseJoinColumns = { @JoinColumn(name = "customer_tag_id") })
	@Fetch(FetchMode.SELECT)
	private Set<CustomerTag> customerTagSet;
	
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "pfdi_category_size_mapping", 
			joinColumns = {@JoinColumn(name = "size_id") },
			inverseJoinColumns = { @JoinColumn(name = "category_id") })
	@Fetch(FetchMode.SELECT)
	private Set<Category> category;

	public Set<CustomerTag> getCustomerTagSet() {
		return customerTagSet;
	}

	public void setCustomerTagSet(Set<CustomerTag> locationTagSet) {
		this.customerTagSet = locationTagSet;
	}
	
	public Set<Category> getCategory() {
		return category;
	}

	public void setCategory(Set<Category> category) {
		this.category = category;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	

	public String getSizeName() {
		return sizeName;
	}

	public void setSizeName(String sizeName) {
		this.sizeName = sizeName;
	}

	public String getSizeDescription() {
		return sizeDescription;
	}

	public void setSizeDescription(String sizeDescription) {
		this.sizeDescription = sizeDescription;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public int hashCode() {
		if (getId() != null) {
			return getId().hashCode();
		}
		return super.hashCode();
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
		Size other = (Size) obj;
		if (getId() == null || other.getId() == null) {
			return false;
		}
		return getId().equals(other.getId());
	}

	public String getSizeCategory() {
		return sizeCategory;
	}

	public void setSizeCategory(String sizeCategory) {
		this.sizeCategory = sizeCategory;
	}

}
