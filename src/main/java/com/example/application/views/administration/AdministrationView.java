package com.example.application.views.administration;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.PfdiLocation;
import com.example.application.data.entity.PfdiPosition;
import com.example.application.data.entity.PfdiRoles;
import com.example.application.data.service.LocationsService;
import com.example.application.data.service.PasswordResetService;
import com.example.application.data.service.PositionsService;
import com.example.application.data.service.RolesService;
import com.example.application.data.service.UserService;
import com.example.application.utils.service.EmailService;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.example.application.views.products.AddNewProductView;
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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("Administration")
@Route(value = "admin/:samplePersonID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "/admin", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN" })
@Uses(Icon.class)
public class AdministrationView extends AbstractPfdiView implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	private Grid<AppUser> grid = new Grid<>(AppUser.class, false);

	private TextField firstName;
	private IntegerField id;
	private TextField lastName;
	private Select<String> position;
	private Select<String> location;
	private Select<String> role;
	private DatePicker startDateOfAccess;
	private DatePicker endDateOfAccess;
	private EmailField emailAddress;

	private Button cancelButton = new Button("Cancel");
	private Button saveButton = new Button("Save");

	private Button addProfileButton;

	private Dialog addProfileDialog = new Dialog();

	private BeanValidationBinder<AppUser> binder;

	private final UserService userService;
	private final RolesService rolesService;
	private final LocationsService locationsService;
	private final PositionsService positionsService;
	private final PasswordResetService passwordResetService;
	ListDataProvider<AppUser> ldp = null;
	private AppUser appUser;
	private EmailService emailService;

	@Autowired
	public AdministrationView(UserService userService, RolesService rolesService, LocationsService locationsService,
			PositionsService positionsService, EmailService emailService, PasswordResetService passwordResetService) {
		super("Admin", "Admin");
		this.userService = userService;
		this.rolesService = rolesService;
		this.locationsService = locationsService;
		this.positionsService = positionsService;
		this.emailService = emailService;
		this.passwordResetService = passwordResetService;
		addClassNames("administration-view");

		VerticalLayout tableContent = new VerticalLayout();
		createGridLayout(tableContent);

		createProfileDialog("Add Profile");

		add(tableContent);

		// Bind fields. This is where you'd define e.g. validation rules
		binder = new BeanValidationBinder<>(AppUser.class);
		binder.bindInstanceFields(this);
		binder.forField(emailAddress)
				.withValidator(email -> validateEmailExists(email) != true,
						"Email address already exists in the system. Please enter a valid email address.")
				.bind(AppUser::getEmailAddress, AppUser::setEmailAddress);

		addProfileButton.addClickListener(e -> {
			this.appUser = null;
			populateDataAndCallDialog();
		});
	}

	private boolean validateEmailExists(String email) {
		AppUser user = userService.findByEmailAddress(email);
		return user != null && !user.equals(this.appUser) ? true : false;
	}

	private void createProfileDialog(String label) {
		Label addProfileLabel = new Label(label);
		addProfileLabel.getStyle().set("padding-bottom", "20px");

		Hr divider1 = new Hr();

		Hr divider2 = new Hr();

//		TextField accountId = new TextField("Account Id");
//		Integer nextValue = userService.getLastId() + 1;
//		accountId.setReadOnly(true);
//		accountId.setValue(nextValue.toString());

		List<PfdiRoles> roles = rolesService.listAll(Sort.unsorted());
		role = new Select<>();
		role.setLabel("Role");
		role.setEmptySelectionAllowed(false);
		role.setItems(roles.stream().map(PfdiRoles::getRole_guid).collect(Collectors.toList()));
		role.setEmptySelectionAllowed(false);
		role.setRequiredIndicatorVisible(true);
		role.setPlaceholder("Select Role");

		firstName = new TextField("Employee First Name");
		firstName.setId("first-name-form");
		firstName.setRequired(true);
		firstName.setRequiredIndicatorVisible(true);

		lastName = new TextField("Employee Last Name");
		lastName.setRequired(true);
		lastName.setRequiredIndicatorVisible(true);

		List<PfdiPosition> positions = positionsService.listAll(Sort.unsorted());
		position = new Select<>();
		position.setLabel("Position");
		position.setEmptySelectionAllowed(false);
		position.setItems(positions.stream().map(PfdiPosition::getPosition_name).collect(Collectors.toList()));
		position.getStyle().set("padding-bottom", "20px");
		position.setRequiredIndicatorVisible(true);
		position.setEmptySelectionAllowed(false);
		position.setPlaceholder("Select Position");

		List<PfdiLocation> locations = locationsService.listAll(Sort.unsorted());
		location = new Select<>();
		location.setLabel("Location");
		location.setEmptySelectionAllowed(false);
		location.setItems(locations.stream().map(PfdiLocation::getLocation_short_name).collect(Collectors.toList()));
		location.setRequiredIndicatorVisible(true);
		location.setEmptySelectionAllowed(false);
		location.setPlaceholder("Select Location");

		startDateOfAccess = new DatePicker("Start Date of Access");
		startDateOfAccess.getStyle().set("padding-top", "20px");
		startDateOfAccess.getStyle().set("padding-bottom", "40px");

		endDateOfAccess = new DatePicker("End Date of Access");
		endDateOfAccess.getStyle().set("padding-top", "20px");
		endDateOfAccess.getStyle().set("padding-bottom", "40px");

		emailAddress = new EmailField();
		emailAddress.setLabel("Email Address");
		emailAddress.setRequiredIndicatorVisible(true);

		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(e -> {
			try {

				prepareAppUser();
				binder.writeBean(appUser);
				boolean isNewUser = appUser.getId() == null;

				if (appUser.getUsername() == null) {
					String firstName = appUser.getFirstName().strip().toLowerCase();
					String lastName = appUser.getLastName().strip().toLowerCase();
					String userName = firstName + "." + lastName;
					appUser.setUsername(userName.replace(' ', '.'));
				}

				AppUser updatedAppUser = userService.update(appUser);
				addProfileDialog.close();
				if (isNewUser) {

					try {
						
						String message = passwordResetService.composeResetPasswordMessage(updatedAppUser);
						emailService.sendMail("No Reply: Set Password", message,
								updatedAppUser.getEmailAddress(), updatedAppUser.getFirstName() + " " + updatedAppUser.getLastName());
					} catch (Exception e1) {
						e1.printStackTrace();
						System.out.println(e1.getMessage());
						Notification.show("Could not send the email. Please check with the Administrator.")
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
					}
				}
				clearForm();
				refreshGrid(updatedAppUser);

				Notification.show("Profile successfully created/updated")
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				UI.getCurrent().navigate(AdministrationView.class);
			} catch (ValidationException validationException) {
				Notification.show("An exception happened while trying to store the samplePerson details.");
			}
		});

		cancelButton = new Button("Cancel");
		cancelButton.addClickListener(e -> {
			addProfileDialog.close();
			clearForm();
			refreshGrid();
		});

		id = new IntegerField("Account Id");
		id.setVisible(false);

		FormLayout formLayout = new FormLayout();
		formLayout.setWidth("800px");
		formLayout.add(addProfileLabel, divider1, emailAddress, role, firstName, location, lastName, position, divider2,
				startDateOfAccess, endDateOfAccess, saveButton, cancelButton, id);

		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		formLayout.setColspan(addProfileLabel, 2);
		formLayout.setColspan(divider1, 2);
		formLayout.setColspan(divider2, 2);

		addProfileDialog.add(formLayout);

	}

	private void createTableFunctions(VerticalLayout tableFunctions) {

		addProfileButton = new Button("Add Profile");
		addProfileButton.setClassName(CssClassNamesConstants.GENERIC_BUTTON_CLASS);

		FlexLayout flexWrapper = new FlexLayout();
		flexWrapper.setFlexDirection(FlexDirection.ROW);
		flexWrapper.setJustifyContentMode(JustifyContentMode.END);
		flexWrapper.setClassName("button-layout");
		flexWrapper.add(addProfileButton);

		tableFunctions.add(flexWrapper);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

		List<AppUser> users = userService.listAll(Sort.by("id"));

		if (!users.isEmpty()) {
			users.forEach(user -> populateForm(user));

		} else {
			Notification.show(String.format("No Users available. Please contact your administrator."), 3000,
					Notification.Position.BOTTOM_START);
			refreshGrid();
			event.forwardTo(AdministrationView.class);
		}
	}

	private void createGridLayout(VerticalLayout verticalLayout) {

		Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");
		grid.addColumn(createEmployeeRenderer()).setHeader("Employee").setAutoWidth(true).setFlexGrow(0)
				.setComparator(AppUser::getLastName);
		grid.addColumn("role").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		grid.addColumn("position").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		grid.addColumn("location").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		grid.addColumn(createEmployeeAccessDates()).setHeader("Access Dates").setAutoWidth(true).setFlexGrow(0);

		LitRenderer<AppUser> statusColumnRenderer = LitRenderer
				.<AppUser>of("<vaadin-icon icon='vaadin:${item.icon}' " + "style='width: var(--lumo-icon-size-xs); "
						+ "height: var(--lumo-icon-size-xs); " + "color: ${item.color};'>"
						+ "</vaadin-icon>&nbsp;&nbsp;${item.status}")
				.withProperty("icon", user -> user.getEnabled() ? "circle" : "circle-thin")
				.withProperty("color",
						user -> user.getEnabled() ? "var(--lumo-primary-text-color)"
								: "var(--lumo-disabled-text-color)")
				.withProperty("status", user -> user.getEnabled() ? "Active" : "Inactive");

		grid.addColumn(statusColumnRenderer).setHeader("Status").setSortable(true)
				.setTextAlign(ColumnTextAlign.START);

		grid.addComponentColumn(currentAppUser -> {

			MenuBar menuBar = new MenuBar();
			menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON);
			MenuItem menuItem = menuBar.addItem(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
			menuItem.getElement().setAttribute("aria-label", "More options");
			SubMenu subMenu = menuItem.getSubMenu();
			subMenu.addItem("Edit Profile", e -> {
				this.appUser = currentAppUser;
				populateDataAndCallDialog();

			});
			
			

			subMenu.addItem(currentAppUser.getEnabled() ? "Deactivate" : "Activate", event -> {
				this.appUser = currentAppUser;
				changeStatus();
				currentAppUser.setEnabled(!currentAppUser.getEnabled());

			});

			return menuBar;
		}).setWidth("70px").setFlexGrow(0);

		ldp = DataProvider.ofCollection(userService.listAll(Sort.by("id")));
		
		HorizontalLayout searchFiltersLayout = new HorizontalLayout();
		searchFiltersLayout.addClassName("padding-bottom-medium");
		
		

		GridListDataView<AppUser> dataView = grid.setItems(ldp);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		grid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);

		TextField searchField = new TextField();
		searchField.setPlaceholder("Search by first name, last name or email address");

		Icon searchIcon = new Icon(VaadinIcon.SEARCH);
		searchIcon.setClassName(CssClassNamesConstants.PFDI_ICONS);
		searchField.setSuffixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.addValueChangeListener(e -> dataView.refreshAll());
		searchField.addClassName(CssClassNamesConstants.SEARCH_FILTER_FIELD);
		
		searchFiltersLayout.add(searchField);
		
		

		dataView.addFilter(person -> {
			String searchTerm = searchField.getValue().trim();

			if (searchTerm.isEmpty())
				return true;

			boolean matchesFullName = matchesTerm(person.getFirstName(), searchTerm);
			boolean matchesEmail = matchesTerm(person.getEmailAddress(), searchTerm);
			boolean matchesLastName = matchesTerm(person.getLastName(), searchTerm);

			return matchesFullName || matchesEmail || matchesLastName;
		});
		Hr gridDivider = new Hr();
		gridDivider.addClassName("hr-class-wrapper");
		wrapper.add(searchFiltersLayout,gridDivider,grid);
		verticalLayout.addAndExpand(wrapper);
	}

	private void changeStatus() {
		userService.changeUserStatus(appUser, !appUser.getEnabled());
		refreshGrid(appUser);
		Notification.show("Successfully changed profile status.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		UI.getCurrent().navigate(AdministrationView.class);
	}

	private void populateDataAndCallDialog() {

		if (this.appUser != null) {

			id.setValue(this.appUser.getId());
		}
		binder.readBean(this.appUser);
		addProfileDialog.open();
	}

	private void refreshGrid() {
		grid.select(null);
		grid.getListDataView().refreshAll();
	}

	private void refreshGrid(AppUser appUser) {
		grid.getListDataView().addItem(appUser);
		ldp.refreshItem(appUser);
		refreshGrid();

	}

	private void prepareAppUser() {
		if (appUser == null) {
			appUser = new AppUser();
			appUser.setEnabled(true);
			appUser.setLocked(false);

			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			UUID uuidToken = UUID.randomUUID();
			appUser.setPassword(encoder.encode(uuidToken.toString()));
		}

	}

	private void clearForm() {
		populateForm(null);
	}

	private void populateForm(AppUser value) {
		this.appUser = value;
		// binder.readBean(this.appUser);

	}

	private boolean matchesTerm(String value, String searchTerm) {
		return value.toLowerCase().contains(searchTerm.toLowerCase());
	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContentContainer) {

	}

	@Override
	protected void addChildrenToContentHeaderContainer(VerticalLayout contentHeaderContainer) {
		
		HorizontalLayout headerContainer = new HorizontalLayout();
		headerContainer.setWidthFull();

		FlexLayout headerNameWrapper = new FlexLayout();
		headerNameWrapper.setFlexDirection(FlexDirection.ROW);
		headerNameWrapper.setJustifyContentMode(JustifyContentMode.START);
		headerNameWrapper.setAlignItems(Alignment.CENTER);
		H1 header = new H1("Profiles");
		header.addClassNames("mb-0", "mt-s", "text-xl");
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		FlexLayout flexWrapper = new FlexLayout();
		flexWrapper.setFlexDirection(FlexDirection.ROW);
		flexWrapper.setJustifyContentMode(JustifyContentMode.END);
		flexWrapper.setClassName("button-layout");
		
		addProfileButton = new Button("Add Profile");
		addProfileButton.setClassName(CssClassNamesConstants.GENERIC_BUTTON_CLASS);

		flexWrapper.add(addProfileButton);
		flexWrapper.setWidth("50%");

		headerContainer.add(headerNameWrapper, flexWrapper);
		contentHeaderContainer.add(headerContainer);
	}

	private static Renderer<AppUser> createEmployeeRenderer() {
		return LitRenderer.<AppUser>of("<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
				+ "  <vaadin-avatar img=\"${item.pictureUrl}\" name=\"${item.fullName}\"></vaadin-avatar>"
				+ "  <vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
				+ "    <span> ${item.fullName}</span>"
				+ "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
				+ "     Email: ${item.email}" + "    </span>" 
				+ "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
				+ "     Username: ${item.userName}" + "    </span>" 
				+ "  </vaadin-vertical-layout>" 
				+ "</vaadin-horizontal-layout>")
				.withProperty("pictureUrl", AppUser::getProfilePictureUrl)
					.withProperty("email", AppUser::getEmailAddress)
				.withProperty("fullName", e -> {
					return e.getLastName() + ", " + e.getFirstName();
				})
				.withProperty("userName", AppUser::getUsername);
	}
	
	private static Renderer<AppUser> createEmployeeAccessDates() {
		return LitRenderer.<AppUser>of(
				"<vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
				+ "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
				+ "     Start Date: ${item.startDate}</span>" 
				+ "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
				+ "     End Date: ${item.endDate}</span>" 
				+ "  </vaadin-vertical-layout>")
				.withProperty("startDate", e -> {
					return e.getStartDateOfAccess().toString();
				})
				.withProperty("endDate", e -> {
					LocalDate date = e.getEndDateOfAccess();
					
					return date != null ? e.getEndDateOfAccess().toString() : "N/A";
				});
	}
}