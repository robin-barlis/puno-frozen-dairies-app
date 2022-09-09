package com.example.application.data.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pfdi_roles")
public class PfdiRoles {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer role_id;
	private String role_guid;
	private String role_description;
	private String active_flag;
	
	
	public Integer getRole_id() {
		return role_id;
	}
	public void setRole_id(Integer role_id) {
		this.role_id = role_id;
	}
	public String getRole_guid() {
		return role_guid;
	}
	public void setRole_guid(String role_guid) {
		this.role_guid = role_guid;
	}
	public String getRole_description() {
		return role_description;
	}
	public void setRole_description(String role_description) {
		this.role_description = role_description;
	}
	public String getActive_flag() {
		return active_flag;
	}
	public void setActive_flag(String active_flag) {
		this.active_flag = active_flag;
	}

}
