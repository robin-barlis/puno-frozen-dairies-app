package com.example.application.data.entity.products;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pfdi_location_tag")
public class LocationTag implements Serializable {

	private static final long serialVersionUID = 8250019492935742153L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String locationTagName;


	private String locationTagDescription;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLocationTagDescription() {
		return locationTagDescription;
	}

	public void setLocationTagDescription(String locationTagDescription) {
		this.locationTagDescription = locationTagDescription;
	}

	public String getLocationTagName() {
		return locationTagName;
	}

	public void setLocationTagName(String locationTagName) {
		this.locationTagName = locationTagName;
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
		LocationTag other = (LocationTag) obj;
		if (getId() == null || other.getId() == null) {
			return false;
		}
		return getId().equals(other.getId());
	}

}
