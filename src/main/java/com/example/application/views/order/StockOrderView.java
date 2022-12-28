package com.example.application.views.order;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.PaymentStatus;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.payment.Payment;
import com.example.application.data.service.customers.CustomerService;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
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
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;

@PageTitle("Stock Orders")
@Route(value = "order/stockOrders", layout = MainLayout.class)
@RouteAlias(value = "/order/stockOrders", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN", "Checker", "CHECKER" })
@Uses(Icon.class)
public class StockOrderView extends AbstractPfdiView implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	private Grid<Order> grid = new Grid<>(Order.class, false); Button addCustomerButton;
	
	private OrdersService ordersService;
	private final CustomerService customerService;

	private ListDataProvider<Order> ldp = null;
	private AppUser appUser;

	@Autowired
	public StockOrderView(OrdersService ordersService, CustomerService customerService, AuthenticatedUser user) {
		super("Admin", "Admin");
		this.customerService = customerService;
		this.ordersService = ordersService;
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
		H1 header = new H1("Stock Orders");
		header.addClassNames("mb-0", "mt-s", "text-xl");
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		FlexLayout flexWrapper = new FlexLayout();
		flexWrapper.setFlexDirection(FlexDirection.ROW);
		flexWrapper.setJustifyContentMode(JustifyContentMode.END);
		flexWrapper.setClassName("button-layout");

		addCustomerButton = new Button("Create Stock Order");
		addCustomerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addCustomerButton.setClassName(CssClassNamesConstants.GENERIC_BUTTON_CLASS);
		//addCustomerButton.setVisible(PfdiUtil.isSales(appUser) || PfdiUtil.isSuperUser(appUser));
		addCustomerButton.addClickListener(e -> {

			UI.getCurrent().navigate(CreateOrderFormView.class);
		});
		flexWrapper.add(addCustomerButton);
		flexWrapper.setWidth("50%");

		headerContainer.add(headerNameWrapper, flexWrapper);
		contentHeaderContainer.add(headerContainer);

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

		List<Customer> customers = customerService.listAll(Sort.by("id"));

		if (!customers.isEmpty()) {
			//customers.forEach(customer -> populateForm(customer));

		} else {
			Notification.show(String.format("No customers available. Please contact your administrator."), 3000,
					Notification.Position.BOTTOM_START);
			refreshGrid();
			event.forwardTo(StockOrderView.class);
		}
	}

	private void createGridLayout(VerticalLayout verticalLayout) {

		Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");
		grid.addComponentColumn(order -> {			
			
			VerticalLayout customerLayout = new VerticalLayout();
			customerLayout.setWidthFull();
			customerLayout.setSpacing(false);
			
			Customer customer = order.getCustomer();
			
			HorizontalLayout storeNameLayout = new HorizontalLayout();
			storeNameLayout.setWidthFull();
			Icon icon = new Icon(VaadinIcon.SHOP);
			icon.setClassName("grid-icons");
			Span storeIcon = new Span(icon);
			Span storeNameValue = new Span(customer.getStoreName());
			storeNameValue.setClassName("order-row-value-customer");
			storeNameLayout.add(storeIcon, storeNameValue);
			
			HorizontalLayout ownerNameLayout = new HorizontalLayout();
			ownerNameLayout.setWidthFull();
			Span ownerNameValue = new Span(customer.getOwnerName());
			ownerNameLayout.add(ownerNameValue);
			ownerNameLayout.setClassName("owner-row-secondary-text");
			
			HorizontalLayout storeAddressLayout = new HorizontalLayout();
			storeAddressLayout.setWidthFull();
			Span storeAddressValue = new Span(customer.getAddress());
			storeAddressLayout.add(storeAddressValue);
			storeAddressLayout.setClassName("owner-row-secondary-text");
			
			
			
			customerLayout.add(storeNameLayout, ownerNameLayout, storeAddressLayout);

			return customerLayout;
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Customer").setSortable(true).setComparator(e -> {return e.getCustomer().getStoreName();});

		
		grid.addComponentColumn(order -> {	
			
			VerticalLayout orderLayout = new VerticalLayout();
			orderLayout.setWidthFull();
			orderLayout.setSpacing(false);
			
			
			HorizontalLayout orderIdLayout = new HorizontalLayout();
			orderIdLayout.setWidthFull();
			Span orderIdSpan = new Span("Stock Order:");
			orderIdSpan.setClassName("order-row-label");
			Span orderIdValue = new Span(getStockOrderLink(order));
			orderIdValue.setClassName("order-row-value");
			
			orderIdLayout.add(orderIdSpan, orderIdValue);
			
			
			HorizontalLayout deliveryReceiptLayout = new HorizontalLayout();
			deliveryReceiptLayout.setWidthFull();
			Span deliveryReceiptLabel = new Span("Delivery Receipt:");
			deliveryReceiptLabel.setClassName("order-row-label");
			Span deliveryReceiptValue = new Span(getDeliveryReceiptLink(order));
			deliveryReceiptValue.setClassName("order-row-value");
			
			deliveryReceiptLayout.add(deliveryReceiptLabel, deliveryReceiptValue);
			
			
			boolean isCompanyOwned = PfdiUtil.isRelativeOrCompanyOwned(order.getCustomer().getCustomerTagId());
			
			
			HorizontalLayout invoiceLayout = new HorizontalLayout();
			String label = isCompanyOwned ?"Stock Transfer:" : "Invoice:";
			invoiceLayout.setWidthFull();
			Span invoiceLayoutLabel = new Span(label);
			invoiceLayoutLabel.setClassName("order-row-label");
			Span invoiceLayoutValue = new Span(getInvoiceLink(order, isCompanyOwned));
			invoiceLayoutValue.setClassName("order-row-value");
					
			invoiceLayout.add(invoiceLayoutLabel, invoiceLayoutValue);
			
			orderLayout.add(orderIdLayout,deliveryReceiptLayout, invoiceLayout);
			return orderLayout;
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Order").setSortable(true).setComparator(Order::getStockOrderNumber);

		grid.addComponentColumn(order -> {			
			
			VerticalLayout orderDetailsLayout = new VerticalLayout();
			orderDetailsLayout.setWidthFull();
			orderDetailsLayout.setHeightFull();
			orderDetailsLayout.setSpacing(false);
			
			
			HorizontalLayout amountDueLayout = new HorizontalLayout();
			amountDueLayout.setWidthFull();
			Span amountDueValue = new Span(PfdiUtil.getFormatter().format(order.getAmountDue()));
			amountDueValue.setClassName("order-row-value-amount");
			amountDueLayout.add(amountDueValue);
			
			HorizontalLayout orderedFrom = new HorizontalLayout();
			orderedFrom.setWidthFull();
			Span orderedFromValue = new Span("Sales Rep: " + order.getCreatedByUser().getFirstName() + " " + order.getCreatedByUser().getLastName());
			orderedFrom.add(orderedFromValue);
			orderedFrom.setClassName("owner-row-secondary-text");
			
			HorizontalLayout orderDate = new HorizontalLayout();
			orderDate.setWidthFull();
			Span storeAddressValue = new Span("Order Date : " + order.getCreationDate().toLocalDate());
			orderDate.add(storeAddressValue);
			orderDate.setClassName("owner-row-secondary-text");
			
			
			
			orderDetailsLayout.add(amountDueLayout, orderedFrom, orderDate);

			return orderDetailsLayout;
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START)
			.setHeader("Order Details").setSortable(true)
			.setComparator(e -> {
				return e.getAmountDue();
			}
		);

			
		grid.addComponentColumn(order -> {			
			
			VerticalLayout paymentDetailsLayout = new VerticalLayout();
			paymentDetailsLayout.setWidthFull();
			paymentDetailsLayout.setSpacing(false);
			
			Customer customer = order.getCustomer();
			
			List<Payment> payments = order.getPayments();
			
			HorizontalLayout paymentStatusWrapper = new HorizontalLayout();
			paymentStatusWrapper.setWidthFull();
			
			
			PaymentStatus paymentStatus = PaymentStatus.UNPAID;
			
		
			if (payments.isEmpty()) {
				paymentStatus = PaymentStatus.UNPAID;
			} else  {
				boolean hasForVerification = payments.stream().anyMatch(e -> PaymentStatus.FOR_VERIFICATION.name().equalsIgnoreCase(e.getStatus()));
				if (hasForVerification) {
					
					if (BigDecimal.ZERO.equals(order.getBalance())) {

						paymentStatus = PaymentStatus.FOR_VERIFICATION;
					} else {
						paymentStatus = PaymentStatus.PARTIAL_FOR_VERIFICATION;
					}
				} else if (BigDecimal.ZERO.equals(order.getBalance())) {
					paymentStatus = PaymentStatus.PAID;
				} else {
					paymentStatus = PaymentStatus.PARTIALLY_PAID;
				}
			}
			
			Span paymentStatusBadge = new Span(paymentStatus.getPaymentStatusName());
			paymentStatusBadge.getElement().getThemeList().add(paymentStatus.getBadge());
			paymentStatusWrapper.add( paymentStatusBadge);
			paymentDetailsLayout.add(paymentStatusWrapper);

			if (paymentStatus.equals(PaymentStatus.UNPAID)) {
					
				HorizontalLayout dueDateWrapper = new HorizontalLayout();
				dueDateWrapper.setWidthFull();
				Span dueDateValue = new Span("Due Date: " + order.getDueDate().toString());
				dueDateWrapper.add(dueDateValue);
				dueDateWrapper.setClassName("owner-row-secondary-text");		
				
				paymentDetailsLayout.add(dueDateWrapper);
			} else if (paymentStatus.equals(PaymentStatus.PARTIALLY_PAID)){
				
				HorizontalLayout balanceWrapper = new HorizontalLayout();
				balanceWrapper.setWidthFull();
				Span balanceValue = new Span("Balance: " + order.getBalance());
				balanceWrapper.add(balanceValue);
				balanceWrapper.setClassName("owner-row-secondary-text");	
				
				paymentDetailsLayout.add(balanceWrapper);
				
				HorizontalLayout dueDateWrapper = new HorizontalLayout();
				dueDateWrapper.setWidthFull();
				Span dueDateValue = new Span("Due Date: " + order.getDueDate().toString());
				dueDateWrapper.add(dueDateValue);
				dueDateWrapper.setClassName("owner-row-secondary-text");	
				
				paymentDetailsLayout.add(dueDateWrapper);
				

			}
			
			return paymentDetailsLayout;
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Payment Details").setSortable(true).setComparator(e -> {return e.getCustomer().getStoreName();});
		

				
			

		grid.addColumn("status").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);

		ldp = DataProvider.ofCollection(ordersService.listAll(Sort.by("id")));

		GridListDataView<Order> dataView = grid.setItems(ldp);
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

	private Component getInvoiceLink(Order order, boolean isCompanyOwned) {
		RouteParameters parameters = new RouteParameters("id", order.getId().toString());
		Class<? extends Component> routeClass = isCompanyOwned ? StockTransferView.class : SalesInvoiceView.class;
		
		Integer docNumber = isCompanyOwned ? order.getStockTransferId() : order.getInvoiceId();
		
		String path = "#" + docNumber;
		
		String route = RouteConfiguration.forSessionScope().getUrl(routeClass, parameters);
		Anchor anchor= new Anchor(route, path);
		anchor.setClassName("order-row-value");
		Span span = new Span("N/A");
		span.setClassName("order-row-value");
		Component component = docNumber != null ? anchor  : span;
		return component;

	}

	private Component getDeliveryReceiptLink(Order order) {
		RouteParameters parameters = new RouteParameters("id", order.getId().toString());
		String deliveryReceipt = order.getDeliveryReceiptId() != null ? "#" + order.getDeliveryReceiptId() : "N/A";
		String route = RouteConfiguration.forSessionScope().getUrl(DeliveryReceiptView.class, parameters);
		
		Anchor anchor= new Anchor(route, deliveryReceipt);
		anchor.setClassName("order-row-value");
		Span span = new Span("N/A");
		span.setClassName("order-row-value");
		Component component = order.getDeliveryReceiptId()  != null ? anchor : span;
		return component;
	}

	private Component getStockOrderLink(Order order) {
		RouteParameters parameters = new RouteParameters("id", order.getId().toString());
		String route = RouteConfiguration.forSessionScope().getUrl(StockOrderSummaryView.class, parameters);
       

		Integer stockOrderNumber = order.getStockOrderNumber();
		String pathString = stockOrderNumber != null ? stockOrderNumber.toString() : "N/A";
		String path =  stockOrderNumber != null ? "#" + pathString : "N/A"; ;	
		Anchor component = new Anchor(route, path);
		component.setClassName("order-row-value");
		return component;
	}

	private void refreshGrid() {
		grid.select(null);
		grid.getListDataView().refreshAll();
	}


	@Override
	protected void createMainContentLayout(VerticalLayout mainContentContainer) {

	}

}