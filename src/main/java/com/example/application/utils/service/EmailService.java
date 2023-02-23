package com.example.application.utils.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.springframework.stereotype.Service;

import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

@Service
public class EmailService {

	private static final String TEST_EMAIL = "robbarlis@gmail.com";
	private static final String API_KEY = "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";

	
	public void sendMail(String subject, String message, String toAddress, String toName) throws AddressException, MessagingException, IOException  {
		ApiClient defaultClient = Configuration.getDefaultApiClient();
		// Configure API key authorization: api-key
		ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
		apiKey.setApiKey(API_KEY);

		try {
			TransactionalEmailsApi api = new TransactionalEmailsApi();
			SendSmtpEmailSender sender = new SendSmtpEmailSender();
			sender.setEmail(TEST_EMAIL);
			sender.setName("No Reply");
			
			List<SendSmtpEmailTo> toList = new ArrayList<SendSmtpEmailTo>();
			SendSmtpEmailTo to = new SendSmtpEmailTo();
			to.setEmail(toAddress);
			to.setName(toName);
			toList.add(to);
			
			Properties headers = new Properties();
			headers.setProperty("Some-Custom-Name", "unique-id-1234");
			Properties params = new Properties();
			params.setProperty("parameter", "My param value");
			params.setProperty("subject", "New Subject");
			SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
			sendSmtpEmail.setSender(sender);
			sendSmtpEmail.setTo(toList);
			sendSmtpEmail.setHtmlContent(message);
			sendSmtpEmail.setSubject("Set Password - No Reply");
			sendSmtpEmail.setHeaders(headers);
			sendSmtpEmail.setParams(params);
			CreateSmtpEmail response = api.sendTransacEmail(sendSmtpEmail);
			System.out.println(response.toString());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception occurred:- " + e.getMessage());
		}
	}

}
