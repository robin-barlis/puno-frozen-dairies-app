package com.example.application.views.order.offerings;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.service.customers.CustomerService;
import com.example.application.data.service.products.CustomerTagService;
import com.example.application.data.service.products.LocationTagService;
import com.example.application.data.service.products.ProductService;
import com.example.application.data.service.products.SizesService;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;


@PageTitle("Manage Offerings")
@Route(value = "order/offerings", layout = MainLayout.class)
@RouteAlias(value = "/order/offerings", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser" })
@Uses(Icon.class)
public class OfferingsView extends AbstractPfdiView {

	private static final long serialVersionUID = 1L;

	PaginatedGrid<Order, ?> grid = new PaginatedGrid<>();
	private Button createOfferingButton;

	private final CustomerService customerService;
	private final CustomerTagService customerTagService;
	private final LocationTagService locationTagService;
	private final ProductService productService;
	private final SizesService sizesService;
	

	private ListDataProvider<Order> ldp = null;
	List<Order> orders;
	List<Customer> customers;
	private Dialog searchOrdersDialog = new Dialog();
	private AppUser appUser;

	@Autowired
	public OfferingsView(CustomerService customerService, 
			CustomerTagService customerTagService, 
			LocationTagService locationTagService,
			SizesService sizesService, 
			ProductService productService,
			AuthenticatedUser user)
			 {
		super("Admin", "Admin");
		this.customerService = customerService;
		this.customerTagService = customerTagService;
		this.locationTagService = locationTagService;
		this.sizesService = sizesService;
		this.productService = productService;
		this.appUser = user.get().get();
		addClassNames("administration-view");

		VerticalLayout tableContent = new VerticalLayout();
		createGridLayout(tableContent);

		add(tableContent);

	}

	@Override
	protected void addChildrenToContentHeaderContainer(VerticalLayout contentHeaderContainer) {
		
		HorizontalLayout headerContainer = new HorizontalLayout();
		headerContainer.setWidthFull();

		FlexLayout headerNameWrapper = new FlexLayout();
		headerNameWrapper.setFlexDirection(FlexDirection.ROW);
		headerNameWrapper.setJustifyContentMode(JustifyContentMode.START);
		headerNameWrapper.setAlignItems(Alignment.CENTER);
		H1 header = new H1("Offerings");
		header.addClassNames("mb-0", "mt-s", "text-xl");
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		FlexLayout flexWrapper = new FlexLayout();
		flexWrapper.setFlexDirection(FlexDirection.ROW);
		flexWrapper.setJustifyContentMode(JustifyContentMode.END);
		flexWrapper.setClassName("button-layout");

		createOfferingButton = new Button("Create Offering");
		createOfferingButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		createOfferingButton.setClassName(CssClassNamesConstants.GENERIC_BUTTON_CLASS);
		createOfferingButton.addClickListener(e -> {

			//UI.getCurrent().navigate(CreateOrderFormView.class);
		});
		flexWrapper.add(createOfferingButton);
		flexWrapper.setWidth("50%");

		headerContainer.add(headerNameWrapper, flexWrapper);
		contentHeaderContainer.add(headerContainer);

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

		
	}

	private void createGridLayout(VerticalLayout verticalLayout) {

		Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");
		
		

		verticalLayout.add(wrapper);
	}

	

	private void refreshGrid() {
		if (grid != null) {
			//grid.select(null);
			grid.getListDataView().refreshAll();
		}
	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContentContainer) {

	}

}
