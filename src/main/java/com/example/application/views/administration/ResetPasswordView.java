package com.example.application.views.administration;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.PasswordReset;
import com.example.application.data.service.PasswordResetService;
import com.example.application.data.service.UserService;
import com.example.application.utils.service.EmailService;
import com.example.application.views.login.LoginView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("ResetPassword")
@Route(value = "resetPassword/:token?")
@AnonymousAllowed
public class ResetPasswordView extends VerticalLayout implements HasUrlParameter<String>{

    private static final long serialVersionUID = -1370960994436472023L;
	private UserService userService;
	private EmailService emailService;
	private PasswordResetService passwordResetService;
	private AppUser appuser;
	PasswordReset reset = null;

    public ResetPasswordView(UserService userService, EmailService emailService, PasswordResetService passwordResetService) {
		this.emailService = emailService;
		this.userService = userService;
		this.passwordResetService = passwordResetService;

        

    }

	private void createForm() {
		VerticalLayout resetPasswordFormLayout = new VerticalLayout();
		resetPasswordFormLayout.addClassName("reset-password-container");
		resetPasswordFormLayout.setAlignItems(Alignment.CENTER);
		resetPasswordFormLayout.setJustifyContentMode(JustifyContentMode.CENTER);
		
		H2 header = new H2("Reset Password");
		header.setWidthFull();
		
		
	
		PasswordField passwordField = new PasswordField();
		passwordField.setWidthFull();
		passwordField.setLabel("Password");
		passwordField.setHelperText("Must be at least 8 characters and contains at least one letter and one digit.");
		passwordField.setPattern("^(?=.*[0-9])(?=.*[a-zA-Z]).{8}.*$");
		passwordField.setErrorMessage("Not a valid password");

		
		
		PasswordField confirmPassword = new PasswordField();
		confirmPassword.setWidthFull();
		confirmPassword.setLabel("Confirm Password");

	
	
		Button resetPasswordButton = new Button("Save Password");
		resetPasswordButton.setWidthFull();
		resetPasswordButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		resetPasswordButton.addClickListener(e-> {
			boolean invalid = passwordField.isInvalid();
			
			if (invalid) {
				System.out.println("1");
				Notification.show("Invalid Password. Please set a valid password.")
				.addThemeVariants(NotificationVariant.LUMO_ERROR);
			} else if (!passwordField.getValue().equals(confirmPassword.getValue())) {
				System.out.println("here");
				confirmPassword.setErrorMessage("Passwords do not match. Please set valid passwords.");
				confirmPassword.setInvalid(true);
			} else {
				confirmPassword.setInvalid(false);
				System.out.println("setting password");
				BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
				appuser.setPassword(encoder.encode(passwordField.getValue()));
				
				userService.update(appuser);
				
				if (reset != null) {
					passwordResetService.delete(reset.getId());
				}
				

				Notification.show("Password was set successfully.")
				.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				UI.getCurrent().navigate(LoginView.class);
			}
			
			
		});
		
		Button cancelReset = new Button("Cancel");
		cancelReset.setWidthFull();
		cancelReset.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		cancelReset.addClickListener(e-> {
			UI.getCurrent().navigate(LoginView.class);
		});
		
		
		resetPasswordFormLayout.add(header);
		resetPasswordFormLayout.add(passwordField);
		resetPasswordFormLayout.add(confirmPassword);
		resetPasswordFormLayout.add(resetPasswordButton);
		resetPasswordFormLayout.add(cancelReset);
		
		add(resetPasswordFormLayout);

		addClassName("overlay-class");

		setAlignItems(Alignment.CENTER);
		setJustifyContentMode(JustifyContentMode.CENTER);
	}

	@Override
	public void setParameter(BeforeEvent event, String token) {
		System.out.println("here : " + token);
		
		if (token == null) {
			Notification.show("Invalid link. Click on Forgot Password link to reset your password again.")
			.addThemeVariants(NotificationVariant.LUMO_ERROR);
			UI.getCurrent().navigate(LoginView.class);
		} else {
			reset = passwordResetService.findByToken(token);
			
			if (reset == null) {

				Notification.show("Invalid link. Click on Forgot Password link to reset your password again.")
				.addThemeVariants(NotificationVariant.LUMO_ERROR);
				UI.getCurrent().navigate(LoginView.class);
			} else {
				boolean isExpired = reset.getExpirationDate().isBefore(LocalDateTime.now());
				System.out.println(isExpired);
				
				if (isExpired ) {

					Notification.show("Link has expired. Click on Forgot Password link to reset your password again.")
					.addThemeVariants(NotificationVariant.LUMO_ERROR);
					UI.getCurrent().navigate(LoginView.class);
				} else {
					appuser = reset.getAppUser();
					createForm();
				}				
			}
			
		}
		
	}
}
