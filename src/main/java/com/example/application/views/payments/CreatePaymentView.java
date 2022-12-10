package com.example.application.views.payments;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.DocumentTrackingNumberEnum;
import com.example.application.data.PaymentMode;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;

@PageTitle("Create Payment")
@Route(value = "payments/createPayment", layout = MainLayout.class)
@PermitAll
public class CreatePaymentView extends AbstractPfdiView implements HasComponents, HasStyle {

	private static final long serialVersionUID = -6210105239749320428L;

	private Button cancelButton;
	private Button saveButton;
	
	private AuthenticatedUser authenticatedUser;
	private CustomerService customerService;	
	private OrdersService orderService;
	private PaymentsService paymentsService;
	private BankService bankService;
	
	
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
	private DatePicker paymentDate;
	private BigDecimalField balance;
	private TextArea notes;
	
	// CHEQUE PAYMENTS
	private TextField chequeNumber;
	private ComboBox<Banks> chequeBankName;
	private TextField chequeAccountNumber;
	private DatePicker chequeIssueDate;
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
	

		
	private final List<Order> orders = Lists.newArrayList();
	
	private BeanValidationBinder<Payment> paymentBinder = new BeanValidationBinder<>(Payment.class);
	private BeanValidationBinder<ChequePaymentDetails> chequePaymentBinder = new BeanValidationBinder<>(ChequePaymentDetails.class);
	private BeanValidationBinder<BankRemittancePaymentDetails> onlineRemittancePaymentBinder = new BeanValidationBinder<>(BankRemittancePaymentDetails.class);
	private DocumentTrackingNumberService documentTrackingNumberService;
//	
//	private BeanValidationBinder<CashPaymentDetails> cashPaymentDetailsBinder;
//	
//	private BeanValidationBinder<ChequePaymentDetails> chequePaymentDetailsBinder;
//	
//	private BeanValidationBinder<BankRemittancePaymentDetails> bankRemittanceBinder;
	
	

	@Autowired
	public CreatePaymentView(DocumentTrackingNumberService documentTrackingNumberService, BankService bankService, OrdersService stockOrderService, PaymentsService paymentService, AuthenticatedUser authenticatedUser, CustomerService customerService, ProductService prodcuctService, CategoryService categoryService, AccessAnnotationChecker accessChecker, ItemStockService itemStockService) {
		super("add-new-product", "Create Payment");
		//addClassNames("products-view");
		this.customerService = customerService;
		this.authenticatedUser = authenticatedUser;
		this.orderService = stockOrderService;
		this.paymentsService = paymentService;
		this.bankService = bankService;
		this.banks = bankService.listAll(Sort.by("bankName"));
		this.documentTrackingNumberService = documentTrackingNumberService;
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
			Payment payment = new Payment();
			DocumentTrackingNumber paymentNumber = documentTrackingNumberService.findByType(DocumentTrackingNumberEnum.PAYMENT_NUMBER.name());
			try {
				paymentBinder.writeBean(payment);
				payment.setPaymentNumber(paymentNumber.getNumber());
				payment.setDueDate(payment.getOrderId().getDueDate());
				payment.setPaymentDate(LocalDate.now());
				payment.setStatus("Paid");
				payment.setCreatedBy(authenticatedUser.get().get());
				payment.setCreatedDate(LocalDateTime.now());
				payment.setUpdatedBy(authenticatedUser.get().get());
				payment.setUpdatedDate(LocalDateTime.now());
				
				if (PaymentMode.CASH.equals(paymentMode.getValue())) {
					CashPaymentDetails cashPaymentDetails = new CashPaymentDetails();
					cashPaymentDetails.setCreatedBy(authenticatedUser.get().get());
					cashPaymentDetails.setCreatedDate(LocalDateTime.now());
					cashPaymentDetails.setUpdatedBy(authenticatedUser.get().get());
					cashPaymentDetails.setUpdatedDate(LocalDateTime.now());
					cashPaymentDetails.setPayment(payment);
					cashPaymentDetails.setTotalAmount(payment.getAmount());
					payment.setCashPaymentDetails(cashPaymentDetails);
				} else if (PaymentMode.ONLINE_REMITTANCE.equals(paymentMode.getValue())) {
					BankRemittancePaymentDetails bankRemittancePaymentDetails = new BankRemittancePaymentDetails();
					onlineRemittancePaymentBinder.writeBean(bankRemittancePaymentDetails);
					bankRemittancePaymentDetails.setCreatedBy(authenticatedUser.get().get());
					bankRemittancePaymentDetails.setCreatedDate(LocalDateTime.now());
					bankRemittancePaymentDetails.setUpdatedBy(authenticatedUser.get().get());
					bankRemittancePaymentDetails.setUpdatedDate(LocalDateTime.now());
					payment.setBankRemittanceDetails(bankRemittancePaymentDetails);
					
				} else if (PaymentMode.CHEQUE.equals(paymentMode.getValue())) {
					ChequePaymentDetails chequePaymentDetails = new ChequePaymentDetails();
					chequePaymentBinder.writeBean(chequePaymentDetails);
					chequePaymentDetails.setCreatedBy(authenticatedUser.get().get());
					chequePaymentDetails.setCreatedDate(LocalDateTime.now());
					chequePaymentDetails.setUpdatedBy(authenticatedUser.get().get());
					chequePaymentDetails.setUpdatedDate(LocalDateTime.now());
					payment.setChequePaymentDetails(chequePaymentDetails);			
				}
				
				
				payment = paymentService.update(payment);
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
		
	
		List<Customer> availableCustomers = customerService.listAll(Sort.unsorted());
		
		orders.clear();
		orders.addAll(orderService.findOrdersForPayment());
		
		storeName = new ComboBox<>();
		storeName.setLabel("Store Name");
		storeName.setWidth("50%");
		storeName.setRequired(true);
		storeName.setAllowCustomValue(false);
		storeName.setItems(availableCustomers);
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
				invoiceNumber.setValue(selectedOrder.getInvoiceId().toString());
				deliveryReceiptNumber.setValue(selectedOrder.getDeliveryReceiptId().toString());
				amountDue.setValue(selectedOrder.getAmountDue());
				dueDate.setValue(selectedOrder.getDueDate());	
				paymentAmount.setValue(selectedOrder.getAmountDue());
				
				if (paymentAmount.getValue() != null) {
					BigDecimal amountDue = selectedOrder.getAmountDue();
					balance.setValue(amountDue.subtract(paymentAmount.getValue()));
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
		amountDue.setLabel("Amount Due");	
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
				BigDecimal amountDue = currentOrder.getAmountDue();
				balance.setValue(amountDue.subtract(e.getValue()));
			}
		});
		paymentAmount.setPrefixComponent(new Span("₱"));
		paymentAmount.setRequiredIndicatorVisible(true);
		paymentBinder.forField(paymentAmount).asRequired("Payment Amount must not be empty").bind(Payment::getAmount, Payment::setAmount);
		
		balance = new BigDecimalField();
		balance.setLabel("Balance");
		balance.setReadOnly(true);
		balance.setPrefixComponent(new Span("₱"));
		balance.setRequiredIndicatorVisible(true);
		paymentBinder.forField(balance).bind(Payment::getBalance, Payment::setBalance);
		
		paymentMode = new Select<>();
		paymentMode.setItems(PaymentMode.values());
		paymentMode.setLabel("Payment Mode");
		paymentMode.setItemLabelGenerator(e-> {
			return e.getName();
		});
		paymentMode.addValueChangeListener(e -> {
			if (PaymentMode.CHEQUE.equals(e.getValue())) {	
				PfdiUtil.setVisibility(true, chequeBankName, chequeNumber, chequeAccountNumber, chequeIssueDate);
				PfdiUtil.setVisibility(false, bankName, depositDate, bankAccountNumber, bankAccountName, bankReferenceNumber, salesDateCovered);
			} else if (PaymentMode.ONLINE_REMITTANCE.equals(e.getValue())) {
				PfdiUtil.setVisibility(false, chequeBankName, chequeNumber, chequeAccountNumber, chequeIssueDate);
				PfdiUtil.setVisibility(true, bankName, depositDate, bankAccountNumber, bankAccountName, bankReferenceNumber, salesDateCovered);

			} else {
				PfdiUtil.setVisibility(false, chequeBankName, chequeNumber, chequeAccountNumber, chequeIssueDate);
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
			}, (bean, field) -> bean.setPaymentMode(field.getName()));
		
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
				amountDue, dueDate, paymentAmount, balance, paymentMode, notes, paymentDetailsDivider);	
		content.setColspan(paymentDetailsDivider, 2);
		
		
		
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
			.bind(ChequePaymentDetails::getAccountNumber, ChequePaymentDetails::setAccountName);
		
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
		
		
		content.add(chequeBankName, chequeNumber, chequeAccountNumber, chequeIssueDate);	
		
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
			.bind(BankRemittancePaymentDetails::getAccountNumber, BankRemittancePaymentDetails::setAccountName);
		
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
}