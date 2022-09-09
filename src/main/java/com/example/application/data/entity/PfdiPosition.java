package com.example.application.data.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pfdi_positions")
public class PfdiPosition {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer position_id;
	private String position_name;
	private String position_description;
	private String active_flag;
	
	public Integer getPosition_id() {
		return position_id;
	}
	public void setPosition_id(Integer position_id) {
		this.position_id = position_id;
	}
	public String getPosition_name() {
		return position_name;
	}
	public void setPosition_name(String position_name) {
		this.position_name = position_name;
	}
	public String getPosition_description() {
		return position_description;
	}
	public void setPosition_description(String position_description) {
		this.position_description = position_description;
	}
	public String getActive_flag() {
		return active_flag;
	}
	public void setActive_flag(String active_flag) {
		this.active_flag = active_flag;
	}
	

}
