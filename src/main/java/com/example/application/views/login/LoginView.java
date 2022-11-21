package com.example.application.views.login;

import java.util.Optional;

import com.example.application.data.entity.AppUser;
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.views.administration.AdministrationView;
import com.example.application.views.order.StockOrderView;
import com.example.application.views.products.ProductsView;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

@PageTitle("Login")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private static final long serialVersionUID = -1370960994436472023L;
	private final AuthenticatedUser authenticatedUser;

    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        setAction("login");

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Sign In");
        i18n.getHeader().setDescription("Puno Frozen Dairies, Inc");
        i18n.setAdditionalInformation(null);
        setI18n(i18n);
        

        setForgotPasswordButtonVisible(false);
        setOpened(true);
    }
    
   

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
    	Optional<AppUser> appUserOpt = authenticatedUser.get();
        if (authenticatedUser.get().isPresent()) {
        	AppUser appUser = appUserOpt.get();
        	
         	
        	if (PfdiUtil.isAdmin(appUser) || PfdiUtil.isSuperUser(appUser)) {
        		System.out.println("admin");
                event.forwardTo(AdministrationView.class);
        	}
        	
        	if (PfdiUtil.isSales(appUser)) {
        		System.out.println("sales");
                event.forwardTo(ProductsView.class);
        	}
        	
        	if (PfdiUtil.isChecker(appUser)) {
        		System.out.println("checker");
                event.forwardTo(StockOrderView.class);
        	}
            
            setOpened(false);          
        }

        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
    
   
}
