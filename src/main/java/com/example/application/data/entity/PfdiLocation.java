package com.example.application.data.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pfdi_locations")
public class PfdiLocation {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer location_id;
	private String location_short_name;
	private String location_description;
	private String active_flag;
	public Integer getLocation_id() {
		return location_id;
	}
	public void setLocation_id(Integer location_id) {
		this.location_id = location_id;
	}
	public String getLocation_short_name() {
		return location_short_name;
	}
	public void setLocation_guid(String location_short_name) {
		this.location_short_name = location_short_name;
	}
	public String getLocation_description() {
		return location_description;
	}
	public void setLocation_description(String location_description) {
		this.location_description = location_description;
	}
	public String getActive_flag() {
		return active_flag;
	}
	public void setActive_flag(String active_flag) {
		this.active_flag = active_flag;
	}
	
	

}
