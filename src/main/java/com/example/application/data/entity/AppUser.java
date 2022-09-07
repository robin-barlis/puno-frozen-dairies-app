package com.example.application.data.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
@Entity
@Table(name = "application_user")
public class AppUser implements Serializable {
	
	
    private static final long serialVersionUID = -1669276701943120022L;
    
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
	public Integer getId() {
		return id;
	}
	public void seId(Integer id) {
		this.id = id;
	}
	private String username;
    private String emailAddress;
	private Boolean locked;
	private Boolean enabled;
	private String firstName;
	private String lastName;
	private String location;
	private String position;
//	private Date startDateOfAccess;
//	private Date expirationDate;
    
    private String password;
    
    @Column(columnDefinition = "text[]")
    @Type(type = "com.example.application.data.type.CustomStringArrayType")
    private String[] roles;
    

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String[] getRoles() {
        return roles;
    }
    public void setRoles(String[] roles) {
        this.roles = roles;
    }
//    public byte[] getProfilePicture() {
//        return profilePicture;
//    }
//    public void setProfilePicture(byte[] profilePicture) {
//        this.profilePicture = profilePicture;
//    }
	
	public Boolean getLocked() {
		return locked;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
//	public Date getStartDateOfAccess() {
//		return startDateOfAccess;
//	}
//	public void setStartDateOfAccess(Date startDateOfAccess) {
//		this.startDateOfAccess = startDateOfAccess;
//	}
//	public Date getExpirationDate() {
//		return expirationDate;
//	}
//	public void setExpirationDate(Date expirationDate) {
//		this.expirationDate = expirationDate;
//	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setLocked(Boolean locked) {
		this.locked = locked;
	}
	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

    public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	

}
