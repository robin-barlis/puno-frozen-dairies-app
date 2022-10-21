package com.example.application;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import com.cloudinary.Cloudinary;
import com.cloudinary.SingletonManager;
import com.cloudinary.utils.ObjectUtils;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@NpmPackage(value = "@fontsource/roboto", version = "4.5.0")
@Theme(value = "punofrozendairies")
@PWA(name = "Puno Frozen Dairies", shortName = "Puno Frozen Dairies", offlineResources = {})
@NpmPackage(value = "line-awesome", version = "1.3.0")
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {

    private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		
        // Set Cloudinary instance
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dw2qyhgar", // insert here you cloud name
                "api_key", "931285452789111", // insert here your api code
                "api_secret", "x0GQ3xIprVMP8YL19OQ2EqNs6QU")); // insert here your api secret
        SingletonManager manager = new SingletonManager();
        manager.setCloudinary(cloudinary);
        manager.init();
        SpringApplication.run(Application.class, args);
        

    }

}
