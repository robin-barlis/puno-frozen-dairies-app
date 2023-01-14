package com.example.application.views.order;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.vaadin.klaudeta.PaginatedGrid;
import org.vaadin.klaudeta.PaginatedGrid.PaginationLocation;

import com.example.application.data.PaymentStatus;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.payment.Payment;
import com.example.application.data.service.customers.CustomerService;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.data.service.products.SizesService;
import com.example.application.reports.OrderSummaryReport2;
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
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
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.StreamResource;

import ar.com.fdvs.dj.domain.builders.ColumnBuilderException;
import net.sf.jasperreports.engine.JRException;

@PageTitle("Stock Orders")
@Route(value = "order/stockOrders", layout = MainLayout.class)
@RouteAlias(value = "/order/stockOrders", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN", "Checker", "CHECKER" })
@Uses(Icon.class)
public class StockOrderView extends AbstractPfdiView implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

//	private PaginatedGrid<Order> grid = new PaginatedGrid<Order>();
	PaginatedGrid<Order, ?> grid = new PaginatedGrid<>();
	private Button addCustomerButton;
	
	private OrdersService ordersService;
	private final CustomerService customerService;

	private ListDataProvider<Order> ldp = null;
	private AppUser appUser;
	private OrderSummaryReport2 orderSummaryReport;
	private SizesService sizesService;
	List<Customer> customers;

	@Autowired
	public StockOrderView(OrdersService ordersService, CustomerService customerService, 
			AuthenticatedUser user, OrderSummaryReport2 orderSummaryReport, SizesService sizesService) {
		super("Admin", "Admin");
		this.customerService = customerService;
		this.ordersService = ordersService;
		this.orderSummaryReport = orderSummaryReport;
		this.sizesService = sizesService;
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

		customers = customerService.listAll(Sort.by("id"));

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
		grid.setSelectionMode(SelectionMode.MULTI);
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
			
			HorizontalLayout stockTransferLayout = new HorizontalLayout();

			stockTransferLayout.setWidthFull();
			Span stockTransferLabel = new Span("Stock Transfer:");
			stockTransferLabel.setClassName("order-row-label");
			Span stockTransferValue = new Span(getStockTransferLink(order, isCompanyOwned));
			stockTransferValue.setClassName("order-row-value");
			

			stockTransferLayout.add(stockTransferLabel, stockTransferValue);
			

			orderLayout.add(orderIdLayout,deliveryReceiptLayout,stockTransferLayout);
			if (!isCompanyOwned) {
				HorizontalLayout invoiceLayout = new HorizontalLayout();
				String label = "Invoice:";
				invoiceLayout.setWidthFull();
				Span invoiceLayoutLabel = new Span(label);
				invoiceLayoutLabel.setClassName("order-row-label");
				Span invoiceLayoutValue = new Span(getInvoiceLink(order, isCompanyOwned));
				invoiceLayoutValue.setClassName("order-row-value");
						
				invoiceLayout.add(invoiceLayoutLabel, invoiceLayoutValue);
				
				orderLayout.add(invoiceLayout);
			}

			
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
		
		grid.addColumn(Order::getStatus).setHeader("Status").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		
		grid.addComponentColumn(currentOrder -> {

			MenuBar menuBar = new MenuBar();
			menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON);
			MenuItem menuItem = menuBar.addItem(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
			menuItem.getElement().setAttribute("aria-label", "More options");
			SubMenu subMenu = menuItem.getSubMenu();
			
			
			MenuItem editItemSubMenu = subMenu.addItem("Edit Order", e -> {
				
				RouteParam param = new RouteParam("orderId", currentOrder.getId().toString());
				RouteParameters params = new RouteParameters(param);
				UI.getCurrent().navigate(CreateOrderFormView.class, params);
			});
			
			if (currentOrder.getPayments() != null && BigDecimal.ZERO.equals(currentOrder.getBalance())) {
			//	addPaymentSubMenu.setEnabled(false);
				editItemSubMenu.setEnabled(false);
			}
			
			
			
			if (currentOrder.getPayments() != null && !BigDecimal.ZERO.equals(currentOrder.getBalance())) {
				
			//	addPaymentSubMenu.setEnabled(true);
			}
			
			if (currentOrder.getPayments() != null && !currentOrder.getPayments().isEmpty()) {
				editItemSubMenu.setEnabled(false);
			}
			
			

			return menuBar;
		}).setWidth("70px").setFlexGrow(0);

		ldp = DataProvider.ofCollection(ordersService.listAll(Sort.by("id")));
		
		MenuBar printStockOrderDocs = new MenuBar();
		printStockOrderDocs.setEnabled(false);
        printStockOrderDocs.addThemeVariants(MenuBarVariant.LUMO_ICON, MenuBarVariant.LUMO_TERTIARY);

        Button printAllButton = new Button(new Icon(VaadinIcon.PRINT) );
        printAllButton.setTooltipText("Print All Documents");
        MenuItem printAll = printStockOrderDocs.addItem(printAllButton);
        
        
        
        MenuBar options = new MenuBar();
        options.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON);
        
        MenuItem optionsMenu = options.addItem(new Icon(VaadinIcon.COG));
        SubMenu optionsMenuSubItems = optionsMenu.getSubMenu();
        MenuItem viewOrders = optionsMenuSubItems.addItem("View");
        
        SubMenu viewOrdersSubItem = viewOrders.getSubMenu();
        
        viewOrdersSubItem.addItem("Today's orders");
        viewOrdersSubItem.addItem("This week's orders");
        viewOrdersSubItem.addItem("This month's orders");
        optionsMenuSubItems.add(new Hr());
        MenuItem rowsPerPage = optionsMenuSubItems.addItem("Rows per page");
        
        SubMenu rowsPerPageSubItem = rowsPerPage.getSubMenu();
        rowsPerPageSubItem.addItem("10", e -> grid.setPageSize(5));
        rowsPerPageSubItem.addItem("10", e -> grid.setPageSize(10));
        rowsPerPageSubItem.addItem("15", e -> grid.setPageSize(15));
        rowsPerPageSubItem.addItem("20", e -> grid.setPageSize(20));
        rowsPerPageSubItem.addItem("50", e -> grid.setPageSize(50));

        optionsMenuSubItems.add(new Hr());
//        MenuItem export = optionsMenuSubItems.addItem("Export");
        
        
        
//        stockOrderPrint.addClickListener(e -> {
//        	Set<Order> selectedItems = grid.getSelectedItems();
//        	
//
//        	
//        	
//        	try {
//				byte[] combinedPdf = orderSummaryReport.buildReport(selectedItems, sizesService.listAll(Sort.unsorted()));
//				
//				StreamResource resource = new StreamResource("order.pdf", () -> {
//					return new ByteArrayInputStream(combinedPdf);
//				});
//				//this.getElement().executeJs("printPdf.printPdf($0)", combinedPdf);
//        	
//        	} catch (ColumnBuilderException | ClassNotFoundException | JRException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} 
//        	
//        
//        	
//        	
//        	
//        });

		GridListDataView<Order> dataView = grid.setItems(ldp);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES, GridVariant.MATERIAL_COLUMN_DIVIDERS);
		grid.setVerticalScrollingEnabled(false);
	    grid.addSelectionListener(e-> {
        	Set<Order> selectedItems = grid.getSelectedItems();
        	printStockOrderDocs.setEnabled(selectedItems != null && !selectedItems.isEmpty());
        });
	    
	    // Sets the max number of items to be rendered on the grid for each page
	    grid.setPageSize(10);

	    // Sets how many pages should be visible on the pagination before and/or after the current selected page
	    grid.setPaginatorSize(5);
	    
	    grid.setHeight("90%");

		TextField searchField = new TextField();
		searchField.setPlaceholder("Filter results by order #, owner, or store");

		Icon searchIcon = new Icon(VaadinIcon.FILTER);
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
		
		Button filterButton = new Button("Search", new Icon(VaadinIcon.SEARCH));
		filterButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SUCCESS);
		filterButton.setIconAfterText(true);
		
		HorizontalLayout printButtonContainer = new HorizontalLayout();
		printButtonContainer.setJustifyContentMode(JustifyContentMode.END);
		printButtonContainer.setPadding(false);	
		printButtonContainer.setWidth("50%");
		printButtonContainer.add(printStockOrderDocs, options);

		HorizontalLayout searchFiltersLayout = new HorizontalLayout();
		searchFiltersLayout.addClassName("padding-bottom-medium");
		searchFiltersLayout.setWidth("50%");
		searchFiltersLayout.add(searchField, filterButton);

		HorizontalLayout secondaryActionsLayout = new HorizontalLayout();
		secondaryActionsLayout.setWidthFull();
		secondaryActionsLayout.add(searchFiltersLayout, printButtonContainer);
		
		VerticalLayout searchFields = createSearchLayout();
		
		wrapper.add(secondaryActionsLayout, new Hr(),  grid);
		verticalLayout.addAndExpand(wrapper);
	}

	private VerticalLayout createSearchLayout() {
		VerticalLayout fields = new VerticalLayout();
		
		HorizontalLayout orderFields = new HorizontalLayout();
		
		
		TextField orderId = new TextField("Order ID");
		
		customers = customerService.listAll(Sort.by("id"));
		MultiSelectComboBox<Customer> customersDropdown = new MultiSelectComboBox<>();
		customersDropdown.setLabel("Customer");
		customersDropdown.setItemLabelGenerator(e -> {
			return e.getStoreName();
		});
		customersDropdown.setItems(customers);

		DatePicker orderDateTo = new DatePicker("Order From To");
		orderDateTo.setValue(LocalDate.now());
		
		DatePicker orderDateFrom = new DatePicker("Order From Date");
		orderDateFrom.setValue(LocalDate.now());
		orderDateFrom.addValueChangeListener(e-> {
			
			if (e.getValue() != null) {

				orderDateTo.setMin(orderDateFrom.getValue());
			}
		});
		
		
		HorizontalLayout orderDateFields = new HorizontalLayout();
		orderDateFields.add(orderDateFrom, orderDateTo);
		
		
		
		
		orderFields.add(orderId, customersDropdown, orderDateFields);
		
		
		fields.add(orderFields);
		
		
		
		return fields;
	}

	private Component getStockTransferLink(Order order, boolean isCompanyOwned) {
	RouteParameters parameters = new RouteParameters("id", order.getId().toString());
		
		String path = "#" + order.getStockTransferId();
		
		String route = RouteConfiguration.forSessionScope().getUrl(StockTransferView.class, parameters);
		Anchor anchor= new Anchor(route, path);
		anchor.setClassName("order-row-value");
		Span span = new Span("N/A");
		span.setClassName("order-row-value");
		Component component = order.getStockTransferId() != null ? anchor  : span;
		return component;
	}

	private Component getInvoiceLink(Order order, boolean isCompanyOwned) {
		RouteParameters parameters = new RouteParameters("id", order.getId().toString());
		
		String path = "#" + order.getInvoiceId();
		
		String route = RouteConfiguration.forSessionScope().getUrl(SalesInvoiceView.class, parameters);
		Anchor anchor= new Anchor(route, path);
		anchor.setClassName("order-row-value");
		Span span = new Span("N/A");
		span.setClassName("order-row-value");
		Component component = order.getInvoiceId() != null ? anchor  : span;
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