package com.example.application.views.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.EmptyOrderException;
import com.example.application.IncorrectOrderException;
import com.example.application.data.DiscountType;
import com.example.application.data.DocumentTrackingNumberEnum;
import com.example.application.data.OrderStatus;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.DocumentTrackingNumber;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.orders.OrderItems;
import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.stock.ItemStock;
import com.example.application.data.service.customers.CustomerService;
import com.example.application.data.service.orders.DocumentTrackingNumberService;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.data.service.products.CategoryService;
import com.example.application.data.service.products.ProductService;
import com.example.application.data.service.stock.ItemStockService;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.order.offerings.AbstractOffering;
import com.example.application.views.order.offerings.FixedAmountPercentageDiscount;
import com.google.api.client.util.Lists;
import com.google.common.collect.Iterables;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.spring.annotation.UIScope;

@PageTitle("Create Order")
@Route(value = "order/createOrder/:orderId?", layout = MainLayout.class)
@RouteAlias(value = "/order/createOrder", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN", "Checker", "CHECKER" })
@UIScope
public class CreateOrderFormView extends AbstractPfdiView implements HasComponents, HasStyle, HasUrlParameter<String> {

	private static final long serialVersionUID = -6210105239749320428L;

	private DateTimePicker orderCreationDate;
	private Select<Customer> storeName;
	private Button cancelButton;
	private Button saveAsDraftButton;
	private Button createStockOrderButton;
	private CategoryService categoryService;
	private ProductService productService;
	private DatePicker dueDate;

	private AuthenticatedUser authenticatedUser;
	private CustomerService customerService;
	private ItemStockService itemStockService;
	private TextField ownerName;
	private Button addDiscount;
	private Button viewDiscounts;

	private TextArea notes;

	private OrdersService orderService;
	private ItemOrderCategorySubView itemOrderCategorySubView = null;
	private Order order = null;
	private DocumentTrackingNumberService documentTrackingNumberService;
	List<Customer> availableCustomers = Lists.newArrayList();
	List<AbstractOffering> offerings = Lists.newArrayList();

	private Integer orderId = null;

	@Autowired
	public CreateOrderFormView(OrdersService stockOrderService, AuthenticatedUser authenticatedUser,
			CustomerService customerService, ProductService prodcuctService, CategoryService categoryService,
			AccessAnnotationChecker accessChecker, ItemStockService itemStockService,
			DocumentTrackingNumberService documentTrackingNumberService) {
		super("add-new-product", "Create Stock Order");
		System.out.println("Creating Order Form View");
		this.customerService = customerService;
		this.authenticatedUser = authenticatedUser;
		this.productService = prodcuctService;
		this.categoryService = categoryService;
		this.orderService = stockOrderService;
		this.itemStockService = itemStockService;
		this.documentTrackingNumberService = documentTrackingNumberService;
		VerticalLayout content = new VerticalLayout();
		createMainContent(content);
		add(content);

	}

	private void createMainContent(VerticalLayout content) {
		FormLayout formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		orderCreationDate = new DateTimePicker("Order Date");
		orderCreationDate.setWidth("50%");
		orderCreationDate.setRequiredIndicatorVisible(true);
		orderCreationDate.setValue(LocalDateTime.now());

		dueDate = new DatePicker("Due Date");
		dueDate.setWidth("50%");
		dueDate.setRequiredIndicatorVisible(true);
		dueDate.setValue(LocalDate.now().plusDays(7));

		ownerName = new TextField("Owner Name");
		ownerName.setReadOnly(true);

		ownerName.setWidth("50%");

		VerticalLayout formContent = new VerticalLayout();
		DiscountsDialogPage discountsDialogPage = new DiscountsDialogPage(productService);
		discountsDialogPage.addOpenedChangeListener(e -> {
			AbstractOffering generatedOffering = discountsDialogPage.getGeneratedOffering();
			if (generatedOffering != null) {
				offerings.add(generatedOffering);
			}
			viewDiscounts.setEnabled(offerings != null && !offerings.isEmpty());

		});
		
		
		DiscountsSummaryDialog discountSummary = new DiscountsSummaryDialog(offerings);
		discountSummary.addOpenedChangeListener(e -> {
			if (!e.isOpened()) {
				offerings = discountSummary.getOfferings();
			} else {
				discountSummary.setOfferings(offerings);
			}
		});
		
		Map<String, List<Customer>> customerPerCategory = customerService.listAllByCustomerTag();
		storeName = new Select<>();
		storeName.setLabel("Outlet Name");
		storeName.setWidth("50%");
		storeName.setEmptySelectionAllowed(false);

		for (Entry<String, List<Customer>> entrySet : customerPerCategory.entrySet()) {
			availableCustomers.addAll(entrySet.getValue());
		}

		storeName.setItems(availableCustomers);

		for (Entry<String, List<Customer>> entrySet : customerPerCategory.entrySet()) {

			List<Customer> value = entrySet.getValue();
			if (value != null && !value.isEmpty()) {

				Div divider = new Div();
				divider.add(new H5(entrySet.getKey()));
				divider.add(new Hr());

				storeName.prependComponents(Iterables.get(value, 0), divider);
			}

		}

		storeName.setItemLabelGenerator(e -> {
			return e.getStoreName();
		});
		storeName.getStyle().set("padding-bottom", "20px");
		storeName.setRequiredIndicatorVisible(true);
		storeName.setEmptySelectionAllowed(false);
		storeName.setPlaceholder("Store Name");
		storeName.addValueChangeListener(e -> {
			ownerName.setValue(e.getValue().getOwnerName());

			if (itemOrderCategorySubView != null) {
				formContent.remove(itemOrderCategorySubView);
			}
			addDiscount.setEnabled(true);

			createOrderContent(formContent, e.getValue());
		});

		notes = new TextArea();
		notes.setLabel("Notes");
		notes.setMaxLength(500);
		notes.setValueChangeMode(ValueChangeMode.EAGER);
		notes.addValueChangeListener(e -> {
			e.getSource().setHelperText(e.getValue().length() + "/500");
		});

		HorizontalLayout discountsLayout = new HorizontalLayout();

		addDiscount = new Button("Add Discount");
		addDiscount.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
		addDiscount.setEnabled(false);
		addDiscount.addClickListener(e -> {

			discountsDialogPage.open();

		});

		viewDiscounts = new Button("Discount Summary");
		viewDiscounts.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
		viewDiscounts.setEnabled(false);
		viewDiscounts.addClickListener(e -> {
			discountSummary.open(); 

		});

		discountsLayout.add(addDiscount, viewDiscounts);

		cancelButton = new Button("Cancel");
		cancelButton.addClickListener(e -> {
			UI.getCurrent().navigate(StockOrderView.class);

		});
		
		saveAsDraftButton = new Button("Save As Draft");
		saveAsDraftButton.addClickListener(e -> {
			try {
				saveOrderAsDraft();
			} catch (IncorrectOrderException e1) {
				showInvalidFieldsDialog();
			} catch (EmptyOrderException e1) {
				showEmptyHeaderFields();
			}

		});

		createStockOrderButton = new Button("Create Stock Order");
		createStockOrderButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		createStockOrderButton.addClickListener(e -> {	
			
			try {
				createStockOrder();
			} catch (IncorrectOrderException e1) {
				showInvalidFieldsDialog();
			} catch (EmptyOrderException e1) {
				showEmptyHeaderFields();
			}
		});

		formLayout.add(orderCreationDate, dueDate, storeName, ownerName, notes, discountsLayout);
		content.add(formLayout);
		content.add(new Hr());
		content.add(formContent);

		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setAlignItems(Alignment.END);
		buttonsLayout.setJustifyContentMode(JustifyContentMode.END);
		buttonsLayout.setWidthFull();
		buttonsLayout.setVerticalComponentAlignment(Alignment.END);

		buttonsLayout.add(cancelButton, saveAsDraftButton, createStockOrderButton);
		content.add(buttonsLayout);

	}

	private void showEmptyHeaderFields() {
		ConfirmDialog dialog = new ConfirmDialog();
		dialog.setHeader("Empty Orders");
		dialog.setText("Order cannot be processed as there are no order items. Please enter valid order numbers.");

		dialog.setConfirmText("OK");
		dialog.addConfirmListener(event -> dialog.close());
		dialog.open();
	}

	private void showInvalidFieldsDialog() {
		ConfirmDialog dialog = new ConfirmDialog();
		dialog.setHeader("Invalid fields");
		dialog.setText("Could not create order. Please correct the invalid fields.");

		dialog.setConfirmText("OK");
		dialog.open();
	}

	private void createStockOrder(OrderStatus orderStatus)  throws IncorrectOrderException, EmptyOrderException {
		boolean isOrderNew = order == null;
		order = createNewOrder(isOrderNew);
		order.setStatus(orderStatus.getOrderStatusName());

		if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
			BigDecimal discountAmount = getDiscountAmount();
			order.setDiscount(discountAmount);
			
			if (orderStatus != OrderStatus.DRAFT) {
				if (order.getStockOrderNumber() == null) {
					DocumentTrackingNumber stockOrderNumber = documentTrackingNumberService.findByType(DocumentTrackingNumberEnum.STOCK_ORDER_NUMBER.name());
					Integer currentStockOrderNumber = stockOrderNumber.getNumber() + 1;
					order.setStockOrderNumber(currentStockOrderNumber);
					stockOrderNumber.setNumber(order.getStockOrderNumber());
					documentTrackingNumberService.update(stockOrderNumber);
				}
			}

			order = orderService.update(order);
			List<ItemStock> itemInventorList = order.getOrderItems().stream().map(ord -> ord.getItemInventory()).collect(Collectors.toList());

			if (Objects.nonNull(itemInventorList)) {
				itemStockService.updateAll(itemInventorList);
			}

			RouteParameters parameters = new RouteParameters("id", order.getId().toString());
			Notification.show("Successfully created order.");
			UI.getCurrent().navigate(StockOrderSummaryView.class, parameters);
		} else {
			throw new EmptyOrderException("Empty Orders");
		}
		
	}
	
	private void createStockOrder()  throws IncorrectOrderException, EmptyOrderException {
		createStockOrder(OrderStatus.SUBMITTED);
		
	}

	private void saveOrderAsDraft() throws IncorrectOrderException, EmptyOrderException {
		createStockOrder(OrderStatus.DRAFT);
	}

	private BigDecimal getDiscountAmount() {
		BigDecimal discountAmount = BigDecimal.ZERO;
		for (AbstractOffering offering : offerings) {

			if (offering instanceof FixedAmountPercentageDiscount) {
				FixedAmountPercentageDiscount fixed = (FixedAmountPercentageDiscount) offering;
				if (fixed.getValue() != null) {
					if (DiscountType.FixedAmount == offering.getDiscountType()) {
						discountAmount = discountAmount.add(BigDecimal.valueOf(fixed.getValue()));
					} else if (DiscountType.Percentage == offering.getDiscountType()) {
						BigDecimal totalAmount = order.getAmountDue();
						BigDecimal percentage = BigDecimal.valueOf(fixed.getValue()).scaleByPowerOfTen(-2);

						BigDecimal currentDiscount = percentage.multiply(totalAmount);
						discountAmount = discountAmount.add(currentDiscount);
					}
				}
			}
		}
		return discountAmount;
	}

	private Order createNewOrder(boolean newOrder) throws IncorrectOrderException {

		Set<OrderItems> orderItemSet = new HashSet<>();
		AppUser user = authenticatedUser.get().get();
		if (newOrder) {
			order = new Order();
			order.setCreatedByUser(user);
			order.setCreationDate(orderCreationDate.getValue());
			order.setCustomer(storeName.getValue());

			orderItemSet.addAll(itemOrderCategorySubView.createOrderItems(user, orderCreationDate.getValue(), order));

			order.setOrderItems(orderItemSet);
		} else {
			updateOrderItems(user);
		}

		order.setStatus(OrderStatus.DRAFT.getOrderStatusName());
		order.setUpdatedByUser(user);
		order.setUpdatedDate(orderCreationDate.getValue());
		order.setNotes(notes.getValue());

		order.setAmountDue(itemOrderCategorySubView.getTotalAmount());
		order.setAmountSrp(itemOrderCategorySubView.getTotalSrpAmount());
		order.setBalance(itemOrderCategorySubView.getTotalAmount());

		order.setAmountDue(itemOrderCategorySubView.getTotalAmount());
		order.setAmountSrp(itemOrderCategorySubView.getTotalSrpAmount());
		order.setDueDate(dueDate.getValue());
		order.setBalance(itemOrderCategorySubView.getTotalAmount());
		return order;
	}

	private void updateOrderItems(AppUser user) {
		order.getOrderItems().clear();

		// order = orderService.update(order);

		Set<OrderItems> orderItems = itemOrderCategorySubView.updateOrderItems(user, order);
//		try {
////			Set<OrderItems> newOrders = itemOrderCategorySubView.createOrderItems(user, orderCreationDate.getValue(),
////					order);
//		} catch (IncorrectOrderException e) {
//			e.printStackTrace();
//		}
//		

		order.setOrderItems(orderItems);

		order.getOrderItems().forEach(orderItem -> {
			System.out.println("Quantity = " + orderItem.getQuantity());
		});

	}

	private void createOrderContent(VerticalLayout content, Customer customer) {

		List<Category> categories = categoryService.listAll(Sort.by(Sort.Direction.ASC, "id"));
		Map<String, List<Product>> productCategoryMap = productService.listAllByCategoryName();

		itemOrderCategorySubView = new ItemOrderCategorySubView(categories, customer, productCategoryMap);

		if (order != null) {
			itemOrderCategorySubView.setOrderItems(order.getOrderItems());
		}
		content.add(itemOrderCategorySubView);

	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContent) {

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		RouteParameters params = event.getRouteParameters();
		String orderIdString = params.get("orderId").orElse(null);

		if (orderIdString != null) {
			orderId = Integer.parseInt(orderIdString);
			order = orderService.get(orderId).orElse(null);
			if (order != null) {

				Optional<Customer> optionalCustomer = availableCustomers.stream().filter(
						customer -> customer.getStoreName().equalsIgnoreCase(order.getCustomer().getStoreName()))
						.findFirst();
				storeName.setValue(optionalCustomer.get());
				storeName.setReadOnly(true);

			}
			orderCreationDate.setValue(order.getCreationDate());
			orderCreationDate.setReadOnly(true);

			dueDate.setReadOnly(true);
		}

	}
}