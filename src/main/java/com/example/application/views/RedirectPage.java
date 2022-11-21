package com.example.application.views;

import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.application.data.entity.AppUser;
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.views.administration.AdministrationView;
import com.example.application.views.order.StockOrderView;
import com.example.application.views.products.ProductsView;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("Jumpoff")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "/", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN", "Sales", "SALES", "Checker", "CHECKER" })
@Uses(Icon.class)
public class RedirectPage  extends VerticalLayout implements BeforeEnterObserver {

	private static final long serialVersionUID = 1L;
	private AuthenticatedUser authenticatedUser;
	@Autowired
	public RedirectPage(AuthenticatedUser authenticatedUser) {
		this.authenticatedUser = authenticatedUser;
		
	}
	@Override
	public void beforeEnter(BeforeEnterEvent event) {

    	System.out.println("here");
		Optional<AppUser> appUserOpt = authenticatedUser.get();
        if (authenticatedUser.get().isPresent()) {
        	System.out.println("here");
        	AppUser appUser = appUserOpt.get();
        	
        	if (PfdiUtil.isAdmin(appUser) || PfdiUtil.isSuperUser(appUser)) {
                event.forwardTo(AdministrationView.class);
        	}
        	
        	if (PfdiUtil.isSales(appUser)) {
                event.forwardTo(ProductsView.class);
        	}
        	
        	if (PfdiUtil.isChecker(appUser)) {
                event.forwardTo(StockOrderView.class);
        	}
             
        }
		
	}
	

}
