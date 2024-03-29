package com.example.application.views.payments;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.ChequeStatus;
import com.example.application.data.DocumentTrackingNumberEnum;
import com.example.application.data.PaymentMode;
import com.example.application.data.PaymentStatus;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.DocumentTrackingNumber;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.payment.BankRemittancePaymentDetails;
import com.example.application.data.entity.payment.Banks;
import com.example.application.data.entity.payment.CashPaymentDetails;
import com.example.application.data.entity.payment.ChequePaymentDetails;
import com.example.application.data.entity.payment.Payment;
import com.example.application.data.service.customers.CustomerService;
import com.example.application.data.service.orders.DocumentTrackingNumberService;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.data.service.payment.BankService;
import com.example.application.data.service.payment.PaymentsService;
import com.example.application.data.service.products.CategoryService;
import com.example.application.data.service.products.ProductService;
import com.example.application.data.service.stock.ItemStockService;
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
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

@PageTitle("Create Payment")
@Route(value = "payments/createPayment/:paymentId?", layout = MainLayout.class)
@RouteAlias(value = "payments/createPayment/", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN", "Accounting", "ACCOUNTING", "Sales", "SALES" })
@UIScope
public class CreatePaymentView extends AbstractPfdiView implements HasComponents, HasStyle, HasUrlParameter<String>  {

	private static final long serialVersionUID = -6210105239749320428L;

	private Button cancelButton;
	private Button saveButton;
	
	private CustomerService customerService;	
	private OrdersService orderService;
	
	
	//Order Details
	private ComboBox<Customer> storeName;
	private TextField ownerName;
	private ComboBox<Order> order;
	private DatePicker orderDate;

	private BigDecimalField amountDue;
	private DatePicker dueDate;
	private TextField invoiceNumber;
	private TextField deliveryReceiptNumber;

	//Payment Details
	private Select<PaymentMode> paymentMode;
	private BigDecimalField paymentAmount;
	private BigDecimalField balanceAfterPayment;

	private BigDecimalField currentBalance;
	
	private TextArea notes;
	
	// CHEQUE PAYMENTS
	private TextField chequeNumber;
	private Select<ChequeStatus> chequeStatus;
	private ComboBox<Banks> chequeBankName;
	private TextField chequeAccountNumber;
	private DatePicker chequeIssueDate;

	private DatePicker chequeDueDate;
	private TextField chequeAccountName;
	
	// Online Remittance
	private ComboBox<Banks>  bankName;
	private DatePicker depositDate;
	private DatePicker salesDateCovered;
	private TextField bankAccountName;
	private TextField bankReferenceNumber;
	private TextField bankAccountNumber;
	
	
	private Order currentOrder;
	
	private List<Banks> banks;
	private PaymentsService paymentService;
	

		
	private final List<Order> orders = Lists.newArrayList();
	
	private BeanValidationBinder<Payment> paymentBinder = new BeanValidationBinder<>(Payment.class);
	private BeanValidationBinder<ChequePaymentDetails> chequePaymentBinder = new BeanValidationBinder<>(ChequePaymentDetails.class);
	private BeanValidationBinder<BankRemittancePaymentDetails> onlineRemittancePaymentBinder = new BeanValidationBinder<>(BankRemittancePaymentDetails.class);

	private int paymentId;

	private Payment payment;

	private BankRemittancePaymentDetails bankRemittancePaymentDetails;

	private ChequePaymentDetails chequePaymentDetails;

	private CashPaymentDetails cashPaymentDetails;

	
	

	@Autowired
	public CreatePaymentView(DocumentTrackingNumberService documentTrackingNumberService, 
			BankService bankService, 
			OrdersService stockOrderService, 
			PaymentsService paymentService, 
			AuthenticatedUser authenticatedUser, 
			CustomerService customerService, 
			ProductService prodcuctService,
			CategoryService categoryService, 
			AccessAnnotationChecker accessChecker, 
			ItemStockService itemStockService) {
		super("add-new-product", "Create Payment");
		//addClassNames("products-view");
		this.customerService = customerService;
		this.orderService = stockOrderService;
		this.paymentService = paymentService;
		this.banks = bankService.listAll(Sort.by("bankName"));
		FormLayout formLayout = new FormLayout();
		formLayout.setResponsiveSteps(
		        // Use one column by default
		        new ResponsiveStep("0", 1),
		        // Use two columns, if layout's width exceeds 500px
		        new ResponsiveStep("500px",2 ));
		createMainContent(formLayout);
		
		FlexLayout buttonsLayout = new FlexLayout();
		buttonsLayout.setFlexDirection(FlexDirection.ROW);
		buttonsLayout.setJustifyContentMode(JustifyContentMode.END);
		buttonsLayout.setClassName("button-layout payment-footer");
		
		saveButton = new Button("Save");
		saveButton.setClassName("float-right secondary-button-sm");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(e -> {
			if (payment == null) {
				payment = new Payment();
			}
			DocumentTrackingNumber paymentNumber = documentTrackingNumberService.findByType(DocumentTrackingNumberEnum.PAYMENT_NUMBER.name());
			try {
				paymentBinder.writeBean(payment);
				if (paymentNumber == null) {
					paymentNumber = new DocumentTrackingNumber();
					paymentNumber.setNumber(0);
					paymentNumber.setType(DocumentTrackingNumberEnum.PAYMENT_NUMBER.name());
				}
				
				
				payment.setPaymentNumber(paymentNumber.getNumber());
				payment.setDueDate(payment.getOrderId().getDueDate());
				payment.setPaymentDate(LocalDate.now());
				payment.setCreatedBy(authenticatedUser.get().get());
				payment.setCreatedDate(LocalDateTime.now());
				payment.setUpdatedBy(authenticatedUser.get().get());
				payment.setUpdatedDate(LocalDateTime.now());
				
				if (BigDecimal.ZERO.compareTo(balanceAfterPayment.getValue()) == 0) {
					payment.setStatus(PaymentStatus.PAID.name());
				} else {
					payment.setStatus(PaymentStatus.PARTIALLY_PAID.name());
				}
				
				if (PaymentMode.CASH.equals(paymentMode.getValue())) {
					if (cashPaymentDetails == null) {
						cashPaymentDetails = new CashPaymentDetails();
						cashPaymentDetails.setCreatedBy(authenticatedUser.get().get());
						cashPaymentDetails.setCreatedDate(LocalDateTime.now());
					}
					cashPaymentDetails.setUpdatedBy(authenticatedUser.get().get());
					cashPaymentDetails.setUpdatedDate(LocalDateTime.now());
					cashPaymentDetails.setPayment(payment);
					cashPaymentDetails.setTotalAmount(payment.getAmount());
					payment.setCashPaymentDetails(cashPaymentDetails);
					
	
				} else if (PaymentMode.ONLINE_REMITTANCE.equals(paymentMode.getValue())) {
					if (bankRemittancePaymentDetails == null) {
						bankRemittancePaymentDetails = new BankRemittancePaymentDetails();
						bankRemittancePaymentDetails.setCreatedBy(authenticatedUser.get().get());
						bankRemittancePaymentDetails.setCreatedDate(LocalDateTime.now());
					}

					onlineRemittancePaymentBinder.writeBean(bankRemittancePaymentDetails);
					bankRemittancePaymentDetails.setUpdatedBy(authenticatedUser.get().get());
					bankRemittancePaymentDetails.setUpdatedDate(LocalDateTime.now());
					bankRemittancePaymentDetails.setPayment(payment);
					payment.setBankRemittanceDetails(bankRemittancePaymentDetails);
					
				} else if (PaymentMode.CHEQUE.equals(paymentMode.getValue())) {
					if (chequePaymentDetails == null) {
						chequePaymentDetails = new ChequePaymentDetails();
						chequePaymentDetails.setCreatedBy(authenticatedUser.get().get());
						chequePaymentDetails.setCreatedDate(LocalDateTime.now());
					}
					chequePaymentBinder.writeBean(chequePaymentDetails);
					chequePaymentDetails.setUpdatedBy(authenticatedUser.get().get());
					chequePaymentDetails.setUpdatedDate(LocalDateTime.now());
					chequePaymentDetails.setPayment(payment);
					payment.setChequePaymentDetails(chequePaymentDetails);	
					
				}
				
				Order order = payment.getOrderId();
				order.setBalance(balanceAfterPayment.getValue());
				
				payment = paymentService.update(payment);
				order = orderService.update(order);
				UI.getCurrent().navigate(PaymentsView.class);
				
				
				
			} catch (ValidationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
//			
//			UI.getCurrent().navigate(PaymentsView.class);
			System.out.println("Here");
			
		});
		
		cancelButton = new Button("Cancel");

		cancelButton.setClassName("float-right secondary-button-sm");
		cancelButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		cancelButton.addClickListener(e -> {
			UI.getCurrent().navigate(PaymentsView.class);
			
		});
		
		buttonsLayout.add(cancelButton, saveButton);
		
		add(formLayout);
		add(buttonsLayout);
	}

	private void createMainContent(FormLayout content) {
		
	
		ownerName = new TextField();
		ownerName = new TextField("Owner Name");
		ownerName.setReadOnly(true);
		ownerName.setWidth("50%");
		
	
		customerService.listAll(Sort.unsorted());
		
		
		
		orders.clear();
		orders.addAll(orderService.findOrdersForPayment());
		
		Set<Customer> customers = orders.stream().map(e -> e.getCustomer()).collect(Collectors.toSet());
		
		storeName = new ComboBox<>();
		storeName.setLabel("Store Name");
		storeName.setWidth("50%");
		storeName.setRequired(true);
		storeName.setAllowCustomValue(false);
		storeName.setItems(customers);
		storeName.setItemLabelGenerator(e -> e.getStoreName());
		storeName.addValueChangeListener(storeName -> {			
			Customer customer = storeName.getValue();
			
			ownerName.setValue(customer.getOwnerName());
			List<Order> orders = orderService.findReadyForPaymentOrdersByCustomerName(customer.getStoreName());
			
			if (order.getValue() != null && !orders.stream().anyMatch(e-> e.getStockOrderNumber().equals(order.getValue().getStockOrderNumber()))) {
				order.clear();
			}
			order.setItems(orders);			
		});
		
		paymentBinder.forField(storeName).asRequired("Store name must not be empty.")
			.bind(Payment::getCustomer, Payment::setCustomer);
		
		order= new ComboBox<>();
		order.setLabel("Stock Order Number");
		order.setWidth("50%");
		order.setAllowCustomValue(false);
		order.setItems(orders);
		order.setItemLabelGenerator(e -> e.getStockOrderNumber().toString());
		order.setRequired(true);
		paymentBinder.forField(order).asRequired("Stock order must not be empty.").bind(Payment::getOrderId, Payment::setOrderId);
		
		order.addValueChangeListener(orderEvent -> {
			//set all values
			
			Order selectedOrder = orderEvent.getValue();
			if (selectedOrder != null) {
				currentOrder = selectedOrder;
				storeName.setValue(selectedOrder.getCustomer());
				orderDate.setValue(selectedOrder.getCreationDate().toLocalDate());
				
				if (selectedOrder.getInvoiceId() != null) {
					invoiceNumber.setValue(selectedOrder.getInvoiceId().toString());
				}
				
				if (selectedOrder.getDeliveryReceiptId() != null) {
					deliveryReceiptNumber.setValue(selectedOrder.getDeliveryReceiptId().toString());
				}
				amountDue.setValue(selectedOrder.getAmountDue());
				dueDate.setValue(selectedOrder.getDueDate());	
				paymentAmount.setValue(selectedOrder.getBalance());
				currentBalance.setValue(selectedOrder.getBalance());
				
				if (paymentAmount.getValue() != null) {
					BigDecimal balanceDue = currentBalance.getValue();
					balanceAfterPayment.setValue(balanceDue.subtract(paymentAmount.getValue()));
				}
			}
		});
		
		orderDate = new DatePicker();
		orderDate.setLabel("Order Date");
		orderDate.setReadOnly(true);
		
		invoiceNumber = new TextField();
		invoiceNumber.setLabel("Invoice Number");	
		invoiceNumber.setReadOnly(true);
		
		deliveryReceiptNumber = new TextField();
		deliveryReceiptNumber.setLabel("Delivery Receipt Number");	
		deliveryReceiptNumber.setReadOnly(true);
		
		
		amountDue = new BigDecimalField();
		amountDue.setLabel("Total Amount Due");	
		amountDue.setReadOnly(true);
		amountDue.setPrefixComponent(new Span("₱"));
		
		
		dueDate = new DatePicker();	
		dueDate.setLabel("Due Date");
		dueDate.setReadOnly(true);
		
		paymentAmount = new BigDecimalField();
		paymentAmount.setLabel("Payment Amount");
		paymentAmount.setValueChangeMode(ValueChangeMode.LAZY);
		paymentAmount.addValueChangeListener(e -> {
			
			if (currentOrder != null) {
				BigDecimal balanceAmount = currentOrder.getBalance();
				balanceAfterPayment.setValue(balanceAmount.subtract(e.getValue()));
			}
		});
		paymentAmount.setPrefixComponent(new Span("₱"));
		paymentAmount.setRequiredIndicatorVisible(true);
		paymentBinder.forField(paymentAmount).asRequired("Payment Amount must not be empty").bind(Payment::getAmount, Payment::setAmount);
		
		balanceAfterPayment = new BigDecimalField();
		balanceAfterPayment.setLabel("Balance After payment");
		balanceAfterPayment.setReadOnly(true);
		balanceAfterPayment.setPrefixComponent(new Span("₱"));
		balanceAfterPayment.setRequiredIndicatorVisible(true);
		paymentBinder.forField(balanceAfterPayment).bind(Payment::getBalance, Payment::setBalance);
		
		
		currentBalance = new BigDecimalField();
		currentBalance.setLabel("Current Balance");
		currentBalance.setReadOnly(true);
		currentBalance.setPrefixComponent(new Span("₱"));
		currentBalance.setRequiredIndicatorVisible(true);
		
		paymentMode = new Select<>();
		paymentMode.setItems(PaymentMode.values());
		paymentMode.setLabel("Payment Mode");
		paymentMode.setItemLabelGenerator(e-> {
			return e.getName();
		});
		paymentMode.addValueChangeListener(e -> {
			if (PaymentMode.CHEQUE.equals(e.getValue())) {	
				PfdiUtil.setVisibility(true, chequeBankName, chequeNumber, chequeDueDate,chequeAccountNumber, chequeAccountName,chequeIssueDate,chequeStatus);
				PfdiUtil.setVisibility(false, bankName, depositDate, bankAccountNumber, bankAccountName, bankReferenceNumber, salesDateCovered);
			} else if (PaymentMode.ONLINE_REMITTANCE.equals(e.getValue())) {
				PfdiUtil.setVisibility(false, chequeBankName, chequeNumber, chequeDueDate, chequeAccountNumber, chequeAccountName, chequeIssueDate, chequeStatus);
				PfdiUtil.setVisibility(true, bankName, depositDate, bankAccountNumber, bankAccountName, bankReferenceNumber, salesDateCovered);

			} else {
				PfdiUtil.setVisibility(false, chequeBankName, chequeNumber, chequeDueDate, chequeAccountNumber, chequeAccountName, chequeIssueDate, chequeStatus);
				PfdiUtil.setVisibility(false, bankName, depositDate, bankAccountNumber, bankAccountName, bankReferenceNumber, salesDateCovered);
			}
 		});
		paymentBinder.forField(paymentMode).asRequired("Payment mode must not be empty. ")
			.bind(e-> {
				if (e.getPaymentMode() != null) {
					return PaymentMode.valueOf(e.getPaymentMode());
				} else {
					return null;
				}
			}, (bean, field) -> bean.setPaymentMode(field.name()));
		
		Hr paymentDetailsDivider = new Hr();
		
		notes = new TextArea();
		notes.setLabel("Notes");
		notes.setMaxLength(500);
		notes.setValueChangeMode(ValueChangeMode.EAGER);
		notes.addValueChangeListener(e -> {
		    e.getSource()
		            .setHelperText(e.getValue().length() + "/500");
		});
		paymentBinder.forField(notes).bind(Payment::getNote, Payment::setNote);
		

		content.add(storeName, ownerName, order, deliveryReceiptNumber, invoiceNumber, orderDate, 
				amountDue, dueDate, paymentAmount, currentBalance, balanceAfterPayment, paymentMode, notes, paymentDetailsDivider);	
		content.setColspan(paymentDetailsDivider, 2);
		
		
		if (payment != null) {

			paymentBinder.readBean(payment);
		}
		
		
		
		// CHEQUE PAYMENTS
		
		chequeBankName = new ComboBox<>();
		chequeBankName.setItems(banks);
		chequeBankName.setLabel("Bank");
		chequeBankName.setVisible(false);
		chequeBankName.setItemLabelGenerator(e-> {
			return e.getBankName();
		});
		chequePaymentBinder.forField(chequeBankName).asRequired("Bank must be selected.")
			.bind(ChequePaymentDetails::getBankId, ChequePaymentDetails::setBankId);
		
		chequeNumber = new TextField();
		chequeNumber.setLabel("Cheque Number");	
		chequeNumber.setVisible(false);
		chequePaymentBinder.forField(chequeNumber).asRequired("Cheque number must not be empty.")
				.bind(ChequePaymentDetails::getChequeNumber, ChequePaymentDetails::setChequeNumber);
		
		
		chequeAccountNumber = new TextField();
		chequeAccountNumber.setLabel("Account Number");	
		chequeAccountNumber.setVisible(false);
		chequePaymentBinder.forField(chequeAccountNumber).asRequired("Account number must not be empty.")
			.bind(ChequePaymentDetails::getAccountNumber, ChequePaymentDetails::setAccountNumber);
		
		chequeAccountName = new TextField();
		chequeAccountName.setLabel("Account Name");	
		chequeAccountName.setVisible(false);
		chequePaymentBinder.forField(chequeAccountName).asRequired("Account name must not be empty.")
			.bind(ChequePaymentDetails::getAccountName, ChequePaymentDetails::setAccountName);
		
		chequeIssueDate = new DatePicker();
		chequeIssueDate.setLabel("Cheque Issue Date");	
		chequeIssueDate.setVisible(false);
		chequePaymentBinder.forField(chequeIssueDate).asRequired("Cheque issue date must not be empty.")
			.bind(ChequePaymentDetails::getChequeIssueDate, ChequePaymentDetails::setChequeIssueDate);
		
		chequeDueDate = new DatePicker();
		chequeDueDate.setLabel("Cheque Due Date");	
		chequeDueDate.setVisible(false);
		chequePaymentBinder.forField(chequeDueDate).asRequired("Cheque issue date must not be empty.")
			.bind(ChequePaymentDetails::getChequeDueDate, ChequePaymentDetails::setChequeDueDate);
		
		chequeStatus = new Select<ChequeStatus>();
		chequeStatus.setLabel("Cheque Status");	
		chequeStatus.setValue(ChequeStatus.RECEIVED);
		chequeStatus.setVisible(false);
		chequeStatus.setItems(ChequeStatus.values());
		chequeStatus.setItemLabelGenerator(status -> {
			return status.getChequeStatus();
		});
		chequePaymentBinder.forField(chequeStatus).asRequired("Cheque issue date must not be empty.")
			.bind(e-> {
				if (e.getChequeStatus() != null) {
					return ChequeStatus.valueOf(e.getChequeStatus());
				} else {
					return null;
				}
			}, (bean, field) -> bean.setChequeStatus(field.name()));
		
		content.add(chequeBankName, chequeNumber, chequeAccountNumber, chequeAccountName,chequeIssueDate,chequeDueDate, chequeStatus);	
		
		bankName = new ComboBox<>();
		bankName.setItems(banks);
		bankName.setLabel("Bank");
		bankName.setVisible(false);
		bankName.setItemLabelGenerator(e-> {
			return e.getBankName();
		});
		onlineRemittancePaymentBinder.forField(bankName).asRequired("Bank must not be empty.")
			.bind(BankRemittancePaymentDetails::getBankId, BankRemittancePaymentDetails::setBankId);
		
		depositDate = new DatePicker();
		depositDate.setLabel("Deposit Date");	
		depositDate.setVisible(false);
		onlineRemittancePaymentBinder.forField(depositDate).asRequired("Deposit Datenot be empty.")
			.bind(BankRemittancePaymentDetails::getDepositDate, BankRemittancePaymentDetails::setDepositDate);
		
		bankAccountNumber = new TextField();
		bankAccountNumber.setLabel("Account Number");	
		bankAccountNumber.setVisible(false);
		onlineRemittancePaymentBinder.forField(bankAccountNumber).asRequired("Deposit Datenot be empty.")
			.bind(BankRemittancePaymentDetails::getAccountNumber, BankRemittancePaymentDetails::setAccountNumber);
		
		bankAccountName = new TextField();
		bankAccountName.setLabel("Account Name");	
		bankAccountName.setVisible(false);
		onlineRemittancePaymentBinder.forField(bankAccountName).asRequired("Deposit Datenot be empty.")
			.bind(BankRemittancePaymentDetails::getAccountName, BankRemittancePaymentDetails::setAccountName);
		
		bankReferenceNumber = new TextField();
		bankReferenceNumber.setLabel("Reference Number");	
		bankReferenceNumber.setVisible(false);
		onlineRemittancePaymentBinder.forField(bankReferenceNumber).asRequired("Deposit Datenot be empty.")
			.bind(BankRemittancePaymentDetails::getReferenceNumber, BankRemittancePaymentDetails::setReferenceNumber);
		
		salesDateCovered = new DatePicker();
		salesDateCovered.setLabel("Sales Date Covered");	
		salesDateCovered.setVisible(false);
		onlineRemittancePaymentBinder.forField(salesDateCovered).asRequired("Deposit Datenot be empty.")
			.bind(BankRemittancePaymentDetails::getSalesDateCovered, BankRemittancePaymentDetails::setSalesDateCovered);
		
		
		content.add(bankName, depositDate, bankAccountNumber, bankAccountName, bankReferenceNumber, salesDateCovered);	

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
		String paymentIdString = params.get("paymentId").orElse(null);
		
		if (paymentIdString != null) {
			paymentId = Integer.parseInt(paymentIdString);
			payment = paymentService.get(paymentId).orElse(null);
			paymentBinder.readBean(payment);
			
			currentBalance.setValue(payment.getOrderId().getBalance().add(payment.getAmount()));
			BigDecimal balanceDue = currentBalance.getValue();
			balanceAfterPayment.setValue(balanceDue.subtract(paymentAmount.getValue()));
			
			PaymentMode mode = PaymentMode.valueOf(payment.getPaymentMode());
			if (PaymentMode.ONLINE_REMITTANCE.equals(mode)) {
				bankRemittancePaymentDetails = payment.getBankRemittanceDetails();
				onlineRemittancePaymentBinder.readBean(bankRemittancePaymentDetails);
				
			} else if (PaymentMode.CHEQUE.equals(mode)) {
				chequePaymentDetails = payment.getChequePaymentDetails();
				chequePaymentBinder.readBean(chequePaymentDetails);
			} else if (PaymentMode.CASH.equals(mode))	 {

				
				cashPaymentDetails = payment.getCashPaymentDetails();
			}
			
			storeName.setReadOnly(true);
			order.setReadOnly(true);
			payment.getOrderId().setBalance(currentBalance.getValue());
			currentOrder = payment.getOrderId();
		}	
	}
}