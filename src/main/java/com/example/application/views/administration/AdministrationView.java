package com.example.application.views.administration;

import java.util.List;
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
import com.example.application.data.service.PositionsService;
import com.example.application.data.service.RolesService;
import com.example.application.data.service.UserService;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
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
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("Administration")
@Route(value = "admin/:samplePersonID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class AdministrationView extends Div implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;
	private final String SAMPLEPERSON_ID = "samplePersonID";
	private final String SAMPLEPERSON_EDIT_ROUTE_TEMPLATE = "admin/%s/edit";

	private Grid<AppUser> grid = new Grid<>(AppUser.class, false);

	private TextField firstName;
	private TextField lastName;
	private Select<String> position;
	private Select<String> location;
	private Select<String> role;
	private DatePicker startDate;
	private DatePicker expirationDate;
	private EmailField emailAddress;
	
	private Button cancelButton = new Button("Cancel");
	private Button saveButton = new Button("Save");
	
	private Button addProfileButton = new Button("Add Profile");

	private Dialog addProfileDialog = new Dialog();

	private BeanValidationBinder<AppUser> binder;

	private final UserService userService;
	private final RolesService rolesService;
	private final LocationsService locationsServcice;
	private final PositionsService positionsService;
	private AppUser appUser;
	
	@Autowired
	public AdministrationView(UserService userService, RolesService rolesService, LocationsService locationsService, PositionsService positionsService) {
		this.userService = userService;
		this.rolesService = rolesService;
		this.locationsServcice = locationsService;
		this.positionsService = positionsService;
		addClassNames("administration-view");

		// create container for filter and add item button
		VerticalLayout tableFunctions = new VerticalLayout();
		createTableFunctions(tableFunctions);

		// create container for admin content
		VerticalLayout tableContent = new VerticalLayout();
		createGridLayout(tableContent);

		createProfileDialog("Add Profile");

		add(tableFunctions);
		add(new Hr());
		add(tableContent);

		// Configure Form
		binder = new BeanValidationBinder<>(AppUser.class);

		// Bind fields. This is where you'd define e.g. validation rules

		binder.bindInstanceFields(this);
		addProfileButton.addClickListener(e -> {
			addProfileDialog.open();
		});
	}

	private void prepareAppUser(AppUser appUser) {
		String firstName = appUser.getFirstName();
		String lastName = appUser.getLastName();
		
		appUser.setUsername(firstName+ "." + lastName);
		appUser.setEnabled(true);
		appUser.setLocked(false);
		String[] role = {this.role.getValue()};
		
		appUser.setRoles(role);
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();		
		appUser.setPassword(encoder.encode("Password123!"));
		
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
		
		List<PfdiLocation> locations = locationsServcice.listAll(Sort.unsorted());
		location = new Select<>();
		location.setLabel("Location");
		location.setEmptySelectionAllowed(false);	
		location.setItems(locations.stream().map(PfdiLocation::getLocation_short_name).collect(Collectors.toList()));
		location.setRequiredIndicatorVisible(true);
		location.setEmptySelectionAllowed(false);
		location.setPlaceholder("Select Location");

		startDate = new DatePicker("Start Date of Access");
		startDate.getStyle().set("padding-top", "20px");
		startDate.getStyle().set("padding-bottom", "40px");
		expirationDate = new DatePicker("Expiration Date");
		expirationDate.getStyle().set("padding-top", "20px");
		expirationDate.getStyle().set("padding-bottom", "40px");
		
		emailAddress = new EmailField();
		emailAddress.setLabel("Email Address");
		emailAddress.setRequiredIndicatorVisible(true);
		

		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(e -> {
            try {
                AppUser appUser = new AppUser();
                
                binder.writeBean(appUser);
                
                prepareAppUser(appUser);
                userService.update(appUser);
                //samplePersonService.update(this.samplePerson);
                clearForm();
                refreshGrid();

    			addProfileDialog.close();
                Notification.show("New profile successfully created.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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

		FormLayout formLayout = new FormLayout();
		formLayout.setWidth("800px");
		formLayout.add(addProfileLabel, divider1, emailAddress, role, firstName, location, lastName, position, divider2, startDate,
				expirationDate, saveButton, cancelButton);
		formLayout.setResponsiveSteps(
				// Use one column by default
				new ResponsiveStep("0", 1),
				// Use two columns, if layout's width exceeds 500px
				new ResponsiveStep("500px", 2));

		formLayout.setColspan(addProfileLabel, 2);
		formLayout.setColspan(divider1, 2);
		formLayout.setColspan(divider2, 2);
		

		addProfileDialog.add(formLayout);

	}

	private void createTableFunctions(VerticalLayout tableFunctions) {
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setClassName("flex-layout");

		FlexLayout flexWrapper = new FlexLayout();
		flexWrapper.setFlexDirection(FlexDirection.ROW);
		flexWrapper.setJustifyContentMode(JustifyContentMode.END);

		flexWrapper.setClassName("button-layout");
		flexWrapper.add(addProfileButton);
		addProfileButton.setClassName(CssClassNamesConstants.GENERIC_BUTTON_CLASS);

		Icon addUserIcon = FontAwesome.Solid.USER_PLUS.create();
		addUserIcon.setColor("#FFFFFF");
		addProfileButton.setIcon(addUserIcon);

		verticalLayout.add(flexWrapper);
		// flexWrapper.setClassName("button-layout");

		tableFunctions.add(verticalLayout);
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

		grid.addColumn("username").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

		grid.addColumn("firstName").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

		grid.addColumn("lastName").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

		grid.addColumn("emailAddress").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

		grid.addColumn("roles").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

		grid.addColumn("position").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);
		
		grid.addColumn("location").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

		// grid.addColumn("startDateOfAccess").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);;
		// grid.addColumn("expirationDate").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);;

		LitRenderer<AppUser> activeRenderer = LitRenderer.<AppUser>of(
				"<vaadin-icon icon='vaadin:${item.icon}' "
				+ "style='width: var(--lumo-icon-size-xs); "
				+ "height: var(--lumo-icon-size-xs); "
				+ "color: ${item.color};'>"
				+ "</vaadin-icon>&nbsp;&nbsp;${item.status}")
				.withProperty("icon", user -> user.getEnabled() ? "circle" : "circle-thin")
				.withProperty("color", user -> user.getEnabled() ? "var(--lumo-primary-text-color)"
						: "var(--lumo-disabled-text-color)")
				.withProperty("status", user -> user.getEnabled() ? "Active"
						: "Inactive");

		grid.addColumn(activeRenderer).setHeader("Status").setAutoWidth(true).setSortable(true)
				.setTextAlign(ColumnTextAlign.CENTER);
		
		
		grid.addComponentColumn(currentAppUser -> {
		      MenuBar menuBar = new MenuBar();
		      menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON);
		      MenuItem menuItem = menuBar.addItem(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
		      menuItem.getElement().setAttribute("aria-label", "More options");
		      SubMenu subMenu = menuItem.getSubMenu();
		      subMenu.addItem("Edit Profile", e -> populateDateAndCallDialog(currentAppUser));
		      subMenu.addItem("Deactivate", event -> {});
		      return menuBar;
		    }).setWidth("70px").setFlexGrow(0);
		
		
		GridListDataView<AppUser> dataView = grid.setItems(userService.listAll(Sort.by("id")));
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		
		

		HorizontalLayout activeColumnWrapper = new HorizontalLayout();
		activeColumnWrapper.add(FontAwesome.Solid.USER_ASTRONAUT.create());
		Label label = new Label("Labeling can be dangerous");
		activeColumnWrapper.add(label);

		grid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);

		// when a row is selected or deselected, populate form
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				UI.getCurrent().navigate(String.format(SAMPLEPERSON_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
			} else {
				clearForm();
				UI.getCurrent().navigate(AdministrationView.class);
			}
		});
		
		
		TextField searchField = new TextField();
        searchField.setPlaceholder("Search by first name, last name or email address");
        
        Icon searchIcon = new Icon(VaadinIcon.SEARCH);
        searchIcon.setClassName(CssClassNamesConstants.PFDI_ICONS);
        searchField.setSuffixComponent(searchIcon);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());
        searchField.addClassName(CssClassNamesConstants.SEARCH_FILTER_FIELD);

		
        dataView.addFilter(person -> {
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty())
                return true;

            boolean matchesFullName = matchesTerm(person.getFirstName(),
                    searchTerm);
            boolean matchesEmail = matchesTerm(person.getEmailAddress(), searchTerm);
            boolean matchesLastName = matchesTerm(person.getLastName(),
                    searchTerm);

            return matchesFullName || matchesEmail || matchesLastName;
        });
        
		wrapper.add(searchField, grid);
		verticalLayout.addAndExpand(wrapper);
	}

	private void populateDateAndCallDialog(AppUser currentAppUser) {
		binder.readBean(currentAppUser);
		addProfileDialog.open();
	}

	private void refreshGrid() {
		grid.select(null);
		grid.getGenericDataView().refreshAll();
	}

	private void clearForm() {
		populateForm(null);
	}

	private void populateForm(AppUser value) {
		this.appUser = value;
		//binder.readBean(this.appUser);

	}
	
    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }
}
