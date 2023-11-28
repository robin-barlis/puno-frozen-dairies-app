package com.example.application.views.administration;

import java.io.IOException;

import javax.mail.MessagingException;

import com.example.application.data.entity.AppUser;
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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
@PageTitle("Request New Password")
@Route(value = "requestPassword/")
@AnonymousAllowed
public class RequestPasswordView extends VerticalLayout {

    private static final long serialVersionUID = -1370960994436472023L;
	private UserService userService;
	private EmailService emailService;
	private PasswordResetService passwordResetService;

    public RequestPasswordView(UserService userService, EmailService emailService, PasswordResetService passwordResetService) {
		this.emailService = emailService;
		this.userService = userService;
		this.passwordResetService = passwordResetService;
		createForm();
        

    }

	private void createForm() {
		VerticalLayout resetPasswordFormLayout = new VerticalLayout();
		resetPasswordFormLayout.addClassName("reset-password-container");
		resetPasswordFormLayout.setAlignItems(Alignment.CENTER);
		resetPasswordFormLayout.setJustifyContentMode(JustifyContentMode.CENTER);
		
		H2 header = new H2("Reset Password");
		header.setWidthFull();
		
		
	
		EmailField validEmailField = new EmailField();
		validEmailField.setWidthFull();
		validEmailField.setLabel("Email address");
		validEmailField.setErrorMessage("Enter a valid email address");
		validEmailField.setRequiredIndicatorVisible(true);
	
	
		Button resetPasswordButton = new Button("Reset Password");
		resetPasswordButton.setWidthFull();
		resetPasswordButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		resetPasswordButton.addClickListener(e-> {
			
			boolean invalid = false;
			String emailValue = validEmailField.getValue();
			if (emailValue == null) {
				invalid = true;
			} else {
				AppUser user = userService.findByEmailAddress(emailValue);
				
				if (user != null) {
					String message = passwordResetService.composeResetPasswordMessage(user);
					try {
						
						emailService.sendMail("No Reply: Set Password", message, emailValue, user.getFirstName() + " " + user.getLastName());
			

						Notification.show("Please check email for password reset instructions.")
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
						
						UI.getCurrent().navigate(LoginView.class);
					} catch (MessagingException | IOException e1) {
						System.out.println(e1.getMessage());
						Notification.show("Could not send the email. Please check with the Administrator.")
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
					}
				} else {
					validEmailField.setErrorMessage("Email Address entered does not exists in the system.");
					invalid = true;
				}
		
			}
			 
			if (invalid) {
				validEmailField.setInvalid(invalid);
			} 
			
			
		});
		
		Button cancelReset = new Button("Cancel");
		cancelReset.setWidthFull();
		cancelReset.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		cancelReset.addClickListener(e-> {
			UI.getCurrent().navigate(LoginView.class);
		});
		
		
		resetPasswordFormLayout.add(header);
		resetPasswordFormLayout.add(validEmailField);
		resetPasswordFormLayout.add(resetPasswordButton);
		resetPasswordFormLayout.add(cancelReset);
		
		add(resetPasswordFormLayout);

		addClassName("overlay-class");

		setAlignItems(Alignment.CENTER);
		setJustifyContentMode(JustifyContentMode.CENTER);
	}
   
}
