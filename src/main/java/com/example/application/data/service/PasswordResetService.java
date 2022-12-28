package com.example.application.data.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.PasswordReset;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;

@Service
public class PasswordResetService {

	@Autowired
    private final PasswordResetRepository repository;

    @Autowired
    public PasswordResetService(PasswordResetRepository repository) {
        this.repository = repository;
    }

    public Optional<PasswordReset> get(Integer id) {
        return repository.findById(id);
    }
    
    public PasswordReset findByToken(String token) {
        return repository.findByToken(token);
    }
    

    public PasswordReset update(PasswordReset user) {
    	if (user.getId() == null) {
            
            return repository.save(user);
        } else {
        	return repository.save(user);
        }
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public int count() {
        return (int) repository.count();
    }
    
	
	
	public String composeResetPasswordMessage(AppUser user) {
		VaadinRequest vaadinRequest = VaadinService.getCurrentRequest();
		HttpServletRequest httpServletRequest = ((VaadinServletRequest)vaadinRequest).getHttpServletRequest();
		
		
		PasswordReset passwordReset = new PasswordReset();
		passwordReset.setAppUser(user);
		
		
		UUID uuidToken = UUID.randomUUID();
		passwordReset.setToken(uuidToken.toString());
		passwordReset.setExpirationDate(LocalDateTime.now().plusDays(2));
		
		
		passwordReset = update(passwordReset);
		
		
		
		String url = httpServletRequest.getRequestURL().toString() + "resetPassword/" + passwordReset.getToken();
		
		
		System.out.println("UR: + " + url);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<span>Dear " + user.getFirstName() +", </span>");
		sb.append("<br>");
		sb.append("<br>");

		sb.append("<br>");
		sb.append("<span>You recently requested to reset your password for your PFDI Business Management Application account.<span>");
		sb.append("<br>");

		sb.append("<br>");
		sb.append("<span><a href=\"");
		sb.append(url);
		sb.append("\">Click here</a> to reset your password for the username " + user.getUsername() +". This password reset is only valid for the next 48 hours.");
		sb.append("<br>");

		sb.append("<br>");
		sb.append("<span>If you believe that an unauthorized person has accessed your account, please reset your password right away by clicking Change Password by clicking your profile.<span>");
		sb.append("<br>");

		sb.append("<br>");
		sb.append("<span>This is a system-generated e-mail. Please do not reply.<span>");
		
		return sb.toString();
		
	}

}
