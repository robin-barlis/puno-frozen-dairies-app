package com.example.application.views.payments;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.payment.Payment;
import com.example.application.data.service.payment.PaymentsService;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("Payments")
@Route(value = "payments", layout = MainLayout.class)
@RouteAlias(value = "/payments", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN"})
@Uses(Icon.class)
public class PaymentsView extends AbstractPfdiView implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	private Grid<Payment> grid = new Grid<>(Payment.class, false); 
	Button createPaymentButton;
	
	private PaymentsService paymentsService;

	private ListDataProvider<Payment> ldp = null;
	private AppUser appUser;

	@Autowired
	public PaymentsView(PaymentsService paymentsService, AuthenticatedUser user) {
		super("Admin", "Admin");
		this.paymentsService = paymentsService;
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
		H1 header = new H1("Payments");
		header.addClassNames("mb-0", "mt-s", "text-xl");
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		FlexLayout flexWrapper = new FlexLayout();
		flexWrapper.setFlexDirection(FlexDirection.ROW);
		flexWrapper.setJustifyContentMode(JustifyContentMode.END);
		flexWrapper.setClassName("button-layout");

		createPaymentButton = new Button("Create Payment");
		createPaymentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		createPaymentButton.setClassName(CssClassNamesConstants.GENERIC_BUTTON_CLASS);
		//addCustomerButton.setVisible(PfdiUtil.isSales(appUser) || PfdiUtil.isSuperUser(appUser));
		createPaymentButton.addClickListener(e -> {

			UI.getCurrent().navigate(CreatePaymentView.class);
		});
		flexWrapper.add(createPaymentButton);
		flexWrapper.setWidth("50%");

		headerContainer.add(headerNameWrapper, flexWrapper);
		contentHeaderContainer.add(headerContainer);

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

		List<Payment> payments = paymentsService.listAll(Sort.by("id"));

		if (!payments.isEmpty()) {
			//customers.forEach(customer -> populateForm(customer));

		} else {
			Notification.show(String.format("No customers available. Please contact your administrator."), 3000,
					Notification.Position.BOTTOM_START);
			refreshGrid();
			event.forwardTo(PaymentsView.class);
		}
	}

	private void createGridLayout(VerticalLayout verticalLayout) {

		Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");
		
		grid.addColumn("paymentDate").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		
		grid.addColumn(payment -> {			
			return payment.getCustomer().getStoreName();
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Store Name").setSortable(true);
		grid.addColumn(payment -> {			
			return payment.getOrderId().getStockOrderNumber();
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Stock Order").setSortable(true);
		grid.addColumn(payment -> {			
			return payment.getOrderId().getInvoiceId();
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Invoice Number").setSortable(true);
		grid.addColumn(payment -> {			
			return payment.getOrderId().getDeliveryReceiptId();
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Delivery Receipt").setSortable(true);
		
		grid.addColumn("paymentMode").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);

		grid.addColumn("amount").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn("balance").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);
	

		grid.addColumn("note").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		

		ldp = DataProvider.ofCollection(paymentsService.listAll(Sort.by("id")));

		GridListDataView<Payment> dataView = grid.setItems(ldp);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		grid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);

		TextField searchField = new TextField();
		searchField.setPlaceholder("Search by owner name or store name");

		Icon searchIcon = new Icon(VaadinIcon.SEARCH);
		searchIcon.setClassName(CssClassNamesConstants.PFDI_ICONS);
		searchField.setSuffixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.addValueChangeListener(e -> dataView.refreshAll());
		searchField.addClassName(CssClassNamesConstants.SEARCH_FILTER_FIELD);

		dataView.addFilter(customer -> {
			String searchTerm = searchField.getValue().trim();

			if (searchTerm.isEmpty()) {

				return true;
			}
			return true;
		});

		wrapper.add(searchField, grid);
		verticalLayout.addAndExpand(wrapper);
	}

	private void refreshGrid() {
		grid.select(null);
		grid.getListDataView().refreshAll();
	}


	@Override
	protected void createMainContentLayout(VerticalLayout mainContentContainer) {

	}

}
