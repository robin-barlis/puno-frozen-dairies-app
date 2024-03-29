package com.example.application.views.customer;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.vaadin.klaudeta.PaginatedGrid;

import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.products.CustomerTag;
import com.example.application.data.entity.products.LocationTag;
import com.example.application.data.service.customers.CustomerService;
import com.example.application.data.service.products.CustomerTagService;
import com.example.application.data.service.products.LocationTagService;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("Customers")
@Route(value = "customers", layout = MainLayout.class)
@RouteAlias(value = "/customers", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN" })
@Uses(Icon.class)
public class CustomerView extends AbstractPfdiView implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	private PaginatedGrid<Customer, ?> grid = new PaginatedGrid<>();

	private IntegerField id;
	private TextField storeName;
	private TextField tinNumber;
	private TextField address;
	private TextField ownerName;
	private BigDecimalField contactNumber;
	private Select<CustomerTag> customerTag;
	private Select<LocationTag> locationTag;
	private DatePicker contractStartDate;
	private DatePicker contractEndDate;

	private Button cancelButton = new Button("Cancel");
	private Button saveButton = new Button("Save");

	private Button addCustomerButton;

	private Dialog addCustomerDialog = new Dialog();

	private BeanValidationBinder<Customer> binder;

	private final CustomerTagService customerTagService;
	private final CustomerService customerService;

	private ListDataProvider<Customer> ldp = null;
	private Customer customer;

	private Set<LocationTag> locationTags = Collections.emptySet();
	private List<Customer> customers;

	private int currentPage = 1;

	@Autowired
	public CustomerView(CustomerTagService customerTagService, CustomerService customerService,
			LocationTagService locationTagService) {
		super("Admin", "Admin");
		this.customerTagService = customerTagService;
		this.customerService = customerService;
		customers = customerService.listAll(Sort.by("id"));
		addClassNames("administration-view");

		VerticalLayout tableContent = new VerticalLayout();
		createGridLayout(tableContent);

		createProfileDialog("Add New Customer");

		add(tableContent);

		// Bind fields. This is where you'd define e.g. validation rules
		binder = new BeanValidationBinder<>(Customer.class);
		binder.forField(locationTag).asRequired("Please select Location Tag").bind(Customer::getLocationTagId, Customer::setLocationTagId);
		binder.forField(customerTag).asRequired("Please select Customer Tag").bind(Customer::getCustomerTagId, Customer::setCustomerTagId);
		binder.forField(storeName).asRequired("Please enter Store Name").bind(Customer::getStoreName, Customer::setStoreName);
		binder.forField(ownerName).asRequired("Please enter Owner Name").bind(Customer::getOwnerName, Customer::setOwnerName);
		binder.forField(address).asRequired("Please enter Customer Address").bind(Customer::getAddress, Customer::setAddress);
		binder.forField(tinNumber).asRequired("Please enter Customer Tin Number").bind(Customer::getTinNumber, Customer::setTinNumber);
		binder.forField(contactNumber).asRequired("Please enter Customer Contact Number").bind(Customer::getContactNumber, Customer::setContactNumber);
		
		
		
		binder.bindInstanceFields(this);

//		binder.forField(emailAddress).withValidator(email -> validateEmailExists(email) != true,
//				"Email address already exists in the system. Please enter a valid email address.").bind(AppUser::getEmailAddress, AppUser::setEmailAddress);
//	
	}

	private void createProfileDialog(String label) {
		Label addCustomerLabel = new Label(label);
		addCustomerLabel.getStyle().set("padding-bottom", "20px");

		Hr divider1 = new Hr();

		Hr divider2 = new Hr();

		List<CustomerTag> customerTags = customerTagService.listAll(Sort.unsorted());
		locationTag = new Select<>();
		locationTag.setLabel("Location Tag");
		locationTag.setEmptySelectionAllowed(false);
		locationTag.setItemLabelGenerator(LocationTag::getLocationTagName);
		locationTag.getStyle().set("padding-bottom", "20px");
		locationTag.setRequiredIndicatorVisible(true);
		locationTag.setEmptySelectionAllowed(false);
	
		if(!customerTags.isEmpty() && customerTags.get(0) != null) {
			locationTags = customerTags.get(0).getLocationTagSet();
		}
		
		locationTag.setItems(locationTags);
		locationTag.setPlaceholder("Select Location Tag");
		locationTag.setEnabled(false);

		customerTag = new Select<>();
		customerTag.setLabel("Customer Tag");
		customerTag.setEmptySelectionAllowed(false);
		customerTag.setItems(customerTags);
		customerTag.setItemLabelGenerator(CustomerTag::getCustomerTagName);
		customerTag.setEmptySelectionAllowed(false);
		customerTag.setRequiredIndicatorVisible(true);
		customerTag.setPlaceholder("Select Customer Tag");
		customerTag.addValueChangeListener(e -> {
			locationTags = e.getValue().getLocationTagSet();
			locationTags.addAll(locationTags);
			locationTag.setItems(locationTags);
			locationTag.setEnabled(true);

		});

		storeName = new TextField("Store Name");
		storeName.setRequired(true);
		storeName.setRequiredIndicatorVisible(true);

		ownerName = new TextField("Owner Name");
		ownerName.setRequired(true);
		ownerName.setRequiredIndicatorVisible(true);

		address = new TextField("Customer Address");
		address.setRequired(true);
		address.setRequiredIndicatorVisible(true);

		contactNumber = new BigDecimalField("Customer Contact Number");
		contactNumber.setRequiredIndicatorVisible(true);

		tinNumber = new TextField("Tin Number");
		tinNumber.setRequired(true);
		tinNumber.setRequiredIndicatorVisible(true);

		contractStartDate = new DatePicker("Contract Start Date");
		contractStartDate.getStyle().set("padding-top", "20px");
		contractStartDate.getStyle().set("padding-bottom", "40px");

		contractEndDate = new DatePicker("Contract End Date");
		contractEndDate.getStyle().set("padding-top", "20px");
		contractEndDate.getStyle().set("padding-bottom", "40px");
		
		

		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		//saveButton.setEnabled(saveButtonEnabled());
		saveButton.addClickListener(e -> {
			try {
				if (binder.validate().isOk()) {
					prepareCustomer();
					binder.writeBean(customer);
					customer.setLocationTagId(locationTag.getValue());
					customer.setCustomerTagId(customerTag.getValue());
					

					customerService.update(customer);
					clearForm();
					//refreshGrid(updateCustomer);

					updateCustomers();
					addCustomerDialog.close();
					Notification.show("Customer successfully created/updated")
							.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				}
			
			} catch (ValidationException validationException) {
				Notification.show("An exception happened while trying to store the customer Details.");
			}
		});

		cancelButton = new Button("Cancel");
		cancelButton.addClickListener(e -> {
			addCustomerDialog.close();
			clearForm();
			refreshGrid();
		});

		id = new IntegerField("Account Id");
		id.setVisible(false);

		FormLayout formLayout = new FormLayout();
		formLayout.setWidth("800px");
		formLayout.add(addCustomerLabel, divider1, customerTag, locationTag, storeName, ownerName, address,
				contactNumber, tinNumber, divider2, contractStartDate, contractEndDate, saveButton, cancelButton, id);

		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		formLayout.setColspan(addCustomerLabel, 2);
		formLayout.setColspan(divider1, 2);
		formLayout.setColspan(divider2, 2);

		addCustomerDialog.add(formLayout);

	}

	private void prepareCustomer() {
		if (customer == null) {
			customer = new Customer();
		}

	}

	@Override
	protected void addChildrenToContentHeaderContainer(VerticalLayout contentHeaderContainer) {
		HorizontalLayout headerContainer = new HorizontalLayout();
		headerContainer.setWidthFull();

		FlexLayout headerNameWrapper = new FlexLayout();
		headerNameWrapper.setFlexDirection(FlexDirection.ROW);
		headerNameWrapper.setJustifyContentMode(JustifyContentMode.START);
		headerNameWrapper.setAlignItems(Alignment.CENTER);
		H1 header = new H1("Customer List");
		header.addClassNames("mb-0", "mt-s", "text-xl");
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		FlexLayout flexWrapper = new FlexLayout();
		flexWrapper.setFlexDirection(FlexDirection.ROW);
		flexWrapper.setJustifyContentMode(JustifyContentMode.END);
		flexWrapper.setClassName("button-layout");

		addCustomerButton = new Button("Add New Customer");
		addCustomerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addCustomerButton.setClassName(CssClassNamesConstants.GENERIC_BUTTON_CLASS);

		addCustomerButton.addClickListener(e -> {
			this.customer = null;
			populateDataAndCallDialog();
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
			customers.forEach(customer -> populateForm(customer));

		} else {
			Notification.show(String.format("No customers available. Please contact your administrator."), 3000,
					Notification.Position.BOTTOM_START);
			refreshGrid();
			event.forwardTo(CustomerView.class);
		}
	}

	private void createGridLayout(VerticalLayout verticalLayout) {

		Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");

		grid.addColumn(Customer::getStoreName).setHeader("Store Name").setAutoWidth(true)
				.setTextAlign(ColumnTextAlign.START);

		grid.addColumn(Customer::getOwnerName).setHeader("Owner").setAutoWidth(true)
				.setTextAlign(ColumnTextAlign.START);

		grid.addColumn(Customer::getTinNumber).setHeader("Tin Number").setAutoWidth(true)
				.setTextAlign(ColumnTextAlign.START);

		grid.addColumn(customer -> {
			return customer.getCustomerTagId().getCustomerTagName();
		}).setHeader("Customer Tag").setAutoWidth(true);

		grid.addColumn(customer -> {
			return customer.getLocationTagId().getLocationTagName();
		}).setHeader("Location Tag").setAutoWidth(true);

		grid.addColumn(Customer::getAddress).setHeader("Address").setTextAlign(ColumnTextAlign.START);

		grid.addColumn(Customer::getContactNumber).setHeader("Contact Number").setAutoWidth(true)
				.setTextAlign(ColumnTextAlign.START);

		grid.addColumn(Customer::getContractStartDate).setHeader("Contract Start Date").setAutoWidth(true)
				.setTextAlign(ColumnTextAlign.START);

		grid.addColumn(Customer::getContractEndDate).setHeader("Contract End Date").setAutoWidth(true)
				.setTextAlign(ColumnTextAlign.START);

		grid.addComponentColumn(currentCustomer -> {

			MenuBar menuBar = new MenuBar();
			menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON);
			MenuItem menuItem = menuBar.addItem(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
			menuItem.getElement().setAttribute("aria-label", "More options");
			SubMenu subMenu = menuItem.getSubMenu();
			subMenu.addItem("Edit Details", e -> {
				this.customer = currentCustomer;
				currentPage = grid.getPage();
				populateDataAndCallDialog();
			});

			subMenu.addItem("Delete", e -> {
				customerService.delete(currentCustomer.getId());
				clearForm();
				//refreshGrid(currentCustomer, true);
				
				updateCustomers();

				addCustomerDialog.close();
				Notification.show("Customer successfully deleted").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				UI.getCurrent().navigate(CustomerView.class);
			});

			return menuBar;
		}).setWidth("70px").setFlexGrow(0);

		ldp = DataProvider.ofCollection(customers);

		grid.setItems(ldp);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		grid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);

		TextField searchField = new TextField();
		searchField.setPlaceholder("Search by owner name or store name");

		Icon searchIcon = new Icon(VaadinIcon.SEARCH);
		searchIcon.setClassName(CssClassNamesConstants.PFDI_ICONS);
		searchField.setSuffixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.LAZY);
		searchField.addValueChangeListener(e -> filter(searchField.getValue()));
		searchField.addClassName(CssClassNamesConstants.SEARCH_FILTER_FIELD);

//		dataView.addFilter(customer -> {
//			String searchTerm = searchField.getValue().trim();
//
//			if (searchTerm.isEmpty())
//				return true;
//
//			boolean matchesCustomerName = matchesTerm(customer.getStoreName(), searchTerm);
//			boolean matchesOwnerName = matchesTerm(customer.getOwnerName(), searchTerm);
//
//			return matchesCustomerName || matchesOwnerName;
//		});

		wrapper.add(searchField, grid);
		verticalLayout.addAndExpand(wrapper);
	}

	private void populateDataAndCallDialog() {

		if (this.customer != null) {

			id.setValue(this.customer.getId());
		}
		binder.readBean(this.customer);
		addCustomerDialog.open();
	}

	private void refreshGrid() {
		grid.select(null);
		grid.getListDataView().refreshAll();
		grid.setPage(currentPage);
	}

	private void filter(String searchFieldValue) {
		String searchTerm = searchFieldValue.trim();

		

		List<Customer> filteredList = customers.stream().filter(cust -> {
			if (searchTerm.isEmpty()) {
				return true;
			}
			boolean matchesCustomerName = matchesTerm(cust.getStoreName(), searchTerm);
			boolean matchesOwnerName = matchesTerm(cust.getOwnerName(), searchTerm);

			return matchesCustomerName || matchesOwnerName;
		}).toList();
		
		

		ldp = DataProvider.ofCollection(filteredList);

		grid.setItems(ldp);

		refreshGrid();
	}

//	private void refreshGrid(Customer updateCustomer, boolean delete) {
//		if (delete) {
//
//			grid.getListDataView().removeItem(updateCustomer);
//		} else {
//
//			grid.getListDataView().addItem(updateCustomer);
//		}
//		ldp.refreshItem(updateCustomer);
//		refreshGrid();
//
//	}

	private void clearForm() {
		populateForm(null);
	}

	private void populateForm(Customer customers) {
		this.customer = customers;
		// binder.readBean(this.appUser);

	}

	private boolean matchesTerm(String value, String searchTerm) {
		return value.toLowerCase().contains(searchTerm.toLowerCase());
	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContentContainer) {

	}
	
	private void updateCustomers() {
		customers = customerService.listAll(Sort.by("id"));
		ldp = DataProvider.ofCollection(customers);

		grid.setItems(ldp);
		

		refreshGrid();
	}

}
