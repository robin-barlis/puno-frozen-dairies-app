package com.example.application.views.order;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
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

@PageTitle("Manage Invoices")
@Route(value = "orders/invoice", layout = MainLayout.class)
@RouteAlias(value = "orders/invoice", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN" })
@Uses(Icon.class)
public class InvoicesView extends AbstractPfdiView implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	private Grid<Customer> grid = new Grid<>(Customer.class, false);

	private IntegerField id;
	private TextField storeName;
	private TextField address;
	private TextField ownerName;
	private IntegerField contactNumber;
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
	private final LocationTagService locationTagService;
	private final CustomerService customerService;

	private ListDataProvider<Customer> ldp = null;
	private Customer customer;

	private Set<LocationTag> locationTags = Collections.emptySet();

	@Autowired
	public InvoicesView(CustomerTagService customerTagService, CustomerService customerService,
			LocationTagService locationTagService) {
		super("Admin", "Admin");
		this.customerTagService = customerTagService;
		this.customerService = customerService;
		this.locationTagService = locationTagService;
		addClassNames("administration-view");

		VerticalLayout tableContent = new VerticalLayout();
		createGridLayout(tableContent);

		createProfileDialog("Add New Customer");

		add(tableContent);

		// Bind fields. This is where you'd define e.g. validation rules
		binder = new BeanValidationBinder<>(Customer.class);
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
		locationTag.setItems(customerTags.get(0).getLocationTagSet());
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

		contactNumber = new IntegerField("Customer Contact Number");
		contactNumber.setRequiredIndicatorVisible(true);

		contractStartDate = new DatePicker("Contract Start Date");
		contractStartDate.getStyle().set("padding-top", "20px");
		contractStartDate.getStyle().set("padding-bottom", "40px");

		contractEndDate = new DatePicker("Contract End Date");
		contractEndDate.getStyle().set("padding-top", "20px");
		contractEndDate.getStyle().set("padding-bottom", "40px");

		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(e -> {
			try {
				prepareCustomer();
				binder.writeBean(customer);
				Integer locationTagId = locationTag.getValue().getId();
				Integer customerTagId = customerTag.getValue().getId();


				Customer updateCustomer = customerService.update(customer);
				clearForm();
				refreshGrid(updateCustomer);

				addCustomerDialog.close();
				Notification.show("Customer successfully created/updated")
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				UI.getCurrent().navigate(InvoicesView.class);
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
				contactNumber, divider2, contractStartDate, contractEndDate, saveButton, cancelButton, id);

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
			event.forwardTo(InvoicesView.class);
		}
	}

	private void createGridLayout(VerticalLayout verticalLayout) {

		Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");

//		//grid.addColumn("id").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
//		grid.addColumn("username").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
//
//		grid.addColumn(currentAppUser -> {
//			String firstName = currentAppUser.getFirstName();
//			String lastName = currentAppUser.getLastName();
//			String fullName = lastName + ", " + firstName;
//			return fullName;
//		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Name").setSortable(true);

		grid.addColumn("storeName").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);

		grid.addColumn("ownerName").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);


		grid.addColumn("address").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);

		grid.addColumn("contactNumber").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);

		grid.addColumn("contractStartDate").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);

		grid.addColumn("contractEndDate").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);

		grid.addComponentColumn(currentCustomer -> {

			MenuBar menuBar = new MenuBar();
			menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON);
			MenuItem menuItem = menuBar.addItem(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
			menuItem.getElement().setAttribute("aria-label", "More options");
			SubMenu subMenu = menuItem.getSubMenu();
			subMenu.addItem("Edit Details", e -> {
				this.customer = currentCustomer;
				populateDataAndCallDialog();
			});

			subMenu.addItem("Delete", e -> {
				customerService.delete(currentCustomer.getId());
				clearForm();
				refreshGrid(currentCustomer);

				addCustomerDialog.close();
				Notification.show("Customer successfully deleted").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				UI.getCurrent().navigate(InvoicesView.class);
			});

			return menuBar;
		}).setWidth("70px").setFlexGrow(0);

		ldp = DataProvider.ofCollection(customerService.listAll(Sort.by("id")));

		GridListDataView<Customer> dataView = grid.setItems(ldp);
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

			if (searchTerm.isEmpty())
				return true;

			boolean matchesCustomerName = matchesTerm(customer.getStoreName(), searchTerm);
			boolean matchesOwnerName = matchesTerm(customer.getOwnerName(), searchTerm);

			return matchesCustomerName || matchesOwnerName;
		});

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
	}

	private void refreshGrid(Customer updateCustomer, boolean delete) {
		if (delete) {

			grid.getListDataView().removeItem(updateCustomer);
		} else {

			grid.getListDataView().addItem(updateCustomer);
		}
		ldp.refreshItem(updateCustomer);
		refreshGrid();

	}

	private void refreshGrid(Customer updateCustomer) {
		refreshGrid(updateCustomer, false);

	}

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

}
