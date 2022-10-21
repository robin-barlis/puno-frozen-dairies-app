package com.example.application.data.entity;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "application_user")
public class AppUser implements Serializable {

	private static final long serialVersionUID = -1669276701943120022L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String username;
	
	@Email(message = "Must be a valid email Address")
	@NotEmpty(message = "Email Address must not be empty")
	private String emailAddress;
	private Boolean locked;
	private Boolean enabled;
	
	@NotEmpty(message = "First Name must not be empty")
	private String firstName;
	
	@NotEmpty(message = "Last Name must not be empty")
	private String lastName;
	
	@NotEmpty(message = "Location must not be empty")
	private String location;
	
	@NotEmpty(message = "Position must not be empty")
	private String position;
	
	@NotEmpty(message = "Role must not be empty")
	private String role;
	
	private LocalDate startDateOfAccess;
	private LocalDate endDateOfAccess;
	private String profilePictureUrl;

	private String password;

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

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

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public LocalDate getStartDateOfAccess() {
		return startDateOfAccess;
	}

	public void setStartDateOfAccess(LocalDate startDateOfAccess) {
		this.startDateOfAccess = startDateOfAccess;
	}

	public LocalDate getEndDateOfAccess() {
		return endDateOfAccess;
	}

	public void setEndDateOfAccess(LocalDate endDateOfAccess) {
		this.endDateOfAccess = endDateOfAccess;
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
		AppUser other = (AppUser) obj;
		if (getId() == null || other.getId() == null) {
			return false;
		}
		return getId().equals(other.getId());
	}

	public String getProfilePictureUrl() {
		return profilePictureUrl;
	}

	public void setProfilePictureUrl(String profilePictureUrl) {
		this.profilePictureUrl = profilePictureUrl;
	}

}
