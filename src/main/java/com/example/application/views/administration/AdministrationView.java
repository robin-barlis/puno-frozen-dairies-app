package com.example.application.views.administration;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.example.application.data.entity.AppUser;
import com.example.application.data.service.UserService;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

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
	private TextField email;
	private TextField phone;
	private DatePicker dateOfBirth;
	private TextField occupation;
	private Checkbox important;

	private Button cancel = new Button("Cancel");
	private Button save = new Button("Save");
	private Button addProfileButton = new Button("Add Profile");

	private Dialog addProfileDialog = new Dialog();

	private BeanValidationBinder<AppUser> binder;

	private final UserService userService;
	private AppUser appUser;

	@Autowired
	public AdministrationView(UserService userService) {
		this.userService = userService;
		addClassNames("administration-view");

		// create container for filter and add item button
		VerticalLayout tableFunctions = new VerticalLayout();
		createTableFunctions(tableFunctions);

		// create container for admin content
		VerticalLayout tableContent = new VerticalLayout();
		createGridLayout(tableContent);

		createAddProfileDialog();

		add(tableFunctions);
		add(new Hr());
		add(tableContent);

		// Configure Form
		binder = new BeanValidationBinder<>(AppUser.class);

		// Bind fields. This is where you'd define e.g. validation rules

		binder.bindInstanceFields(this);

		cancel.addClickListener(e -> {
			clearForm();
			refreshGrid();
		});

		addProfileButton.addClickListener(e -> {
			addProfileDialog.open();
		});

		save.addClickListener(e -> {
//            try {
//                if (this.samplePerson == null) {
//                    this.samplePerson = new SamplePerson();
//                }
//                binder.writeBean(this.samplePerson);
//                //samplePersonService.update(this.samplePerson);
//                clearForm();
//                refreshGrid();
//                Notification.show("SamplePerson details stored.");
//                UI.getCurrent().navigate(AdministrationView.class);
//            } catch (ValidationException validationException) {
//                Notification.show("An exception happened while trying to store the samplePerson details.");
//            }
		});

	}

	private void createAddProfileDialog() {
		Label addProfileLabel = new Label("Add Profile");
		Hr divider1 = new Hr();
		Hr divider2 = new Hr();

		TextField accountId = new TextField("Account Id");
		TextField role = new TextField("Role");
		TextField firstName = new TextField("Employee First Name");

		TextField position = new TextField("Position");
		TextField lastName = new TextField("Employee Last Name");

		TextField location = new TextField("Employee Last Name");

		DatePicker startDate = new DatePicker("Start Date of Access");
		DatePicker expirationDate = new DatePicker("Expiration Date");
		
		

		Button saveButton = new Button("Save");
		
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickListener(e -> {

			addProfileDialog.close();
		});

		FormLayout formLayout = new FormLayout();
		formLayout.setWidth("800px");
		formLayout.add(addProfileLabel, divider1, accountId, role, firstName, position, lastName, location, divider2, startDate,
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
		wrapper.add(grid);

		grid.addColumn("id").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

		grid.addColumn("firstName").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

		grid.addColumn("lastName").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

		grid.addColumn("emailAddress").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

		grid.addColumn("roles").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

		grid.addColumn("position").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

		// grid.addColumn("startDateOfAccess").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);;
		// grid.addColumn("expirationDate").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);;

		LitRenderer<AppUser> activeRenderer = LitRenderer.<AppUser>of(
				"<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
				.withProperty("icon", important -> important.getEnabled() ? "check" : "minus")
				.withProperty("color", important -> important.getEnabled() ? "var(--lumo-primary-text-color)"
						: "var(--lumo-disabled-text-color)");

		grid.addColumn(activeRenderer).setHeader("Status").setAutoWidth(true).setSortable(true)
				.setTextAlign(ColumnTextAlign.CENTER);

		grid.setItems(query -> userService.list(
				PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
				.stream());
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

		verticalLayout.addAndExpand(wrapper);
	}

	private void refreshGrid() {
		grid.select(null);
		grid.getLazyDataView().refreshAll();
	}

	private void clearForm() {
		populateForm(null);
	}

	private void populateForm(AppUser value) {
		this.appUser = value;
		binder.readBean(this.appUser);

	}
}
