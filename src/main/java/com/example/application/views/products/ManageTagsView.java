package com.example.application.views.products;

import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.hibernate.Hibernate;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.entity.products.CustomerTag;
import com.example.application.data.entity.products.LocationTag;
import com.example.application.data.service.products.CustomerTagService;
import com.example.application.data.service.products.LocationTagService;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.example.application.views.products.components.CustomerTagFormDialog;
import com.example.application.views.products.components.LocationTagFormDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Manage Tags")
@Route(value = "/product/tags", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN" })
@Uses(Icon.class)
public class ManageTagsView extends AbstractPfdiView implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	private Grid<LocationTag> locationTagGrid = new Grid<>(LocationTag.class, false);
	private Grid<CustomerTag> customerTagGrid = new Grid<>(CustomerTag.class, false);

	private final LocationTagService locationTagService;
	private final CustomerTagService customerTagService;
	
	private ListDataProvider<LocationTag> ldp = null;
	private ListDataProvider<CustomerTag> customerTagLdp = null;
	private CustomerTagFormDialog customerTagFormDialog =  null;
	private LocationTagFormDialog locationTagDialog;

	@Autowired
	public ManageTagsView(LocationTagService locationTagService, CustomerTagService customerTagService) {
		super("manage-tags", "Product Management > Manage Tags");

		this.locationTagService = locationTagService;
		this.customerTagService = customerTagService;
		
		VerticalLayout content = new VerticalLayout();
		createMainContent(content);
		add(content);	
	}


	@Override
	public void beforeEnter(BeforeEnterEvent event) {

	
	
	}
	
	protected void createMainContent(VerticalLayout tableContent) {
		
		HorizontalLayout headingWrapper = new HorizontalLayout();
		headingWrapper.setSpacing(true);
		headingWrapper.setPadding(false);
		Label locationTagLabel = new Label("Location Tags");
		locationTagLabel.addClassName(CssClassNamesConstants.PROFILE_DETAILS_LABEL_WRAPPER);
		headingWrapper.add(locationTagLabel);
		customerTagFormDialog = new CustomerTagFormDialog("Add New Customer Tag", customerTagService, locationTagService);
		locationTagDialog =  new LocationTagFormDialog("Add New Tag", locationTagService);
		
		
		tableContent.add(headingWrapper);
		locationTagGrid = new Grid<>(LocationTag.class, false);

		ldp = DataProvider.ofCollection(locationTagService.listAll(Sort.by("id")));
		locationTagGrid.setItems(ldp);

		locationTagGrid.setAllRowsVisible(true);

		locationTagGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		locationTagGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		locationTagGrid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
		locationTagGrid.addColumn("locationTagName").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		locationTagGrid.addColumn("locationTagDescription").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		locationTagGrid.addComponentColumn(currentLocationTagTag -> {

			MenuBar menuBar = new MenuBar();
			menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON);
			MenuItem menuItem = menuBar.addItem(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
			menuItem.getElement().setAttribute("aria-label", "More options");
			SubMenu subMenu = menuItem.getSubMenu();
			subMenu.addItem("Edit Location Tag", e -> {
				try {
					locationTagDialog.setCurrentLocationTagSelectionToBinder(currentLocationTagTag);

					locationTagDialog.open();
				} catch (ValidationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
			subMenu.addItem("Delete", e -> {
				ConfirmDialog confirmDialog = new ConfirmDialog();
				confirmDialog.setCancelable(true);
				confirmDialog.setText("This tag will be permanently deleted. Do you want to proceed?");
				confirmDialog.open();
				
				confirmDialog.addConfirmListener(event -> {
					try {

						locationTagService.delete(currentLocationTagTag.getId());
						locationTagGrid.getListDataView().removeItem(currentLocationTagTag);
						locationTagGrid.getListDataView().refreshAll();
						Notification.show("Location Tag successfully deleted.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					} catch (Exception exception) {
						System.out.println(exception.getMessage());
						Notification.show("Unable to delete Location Tag because it is still being used in the Customer Tag.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
						
					}
				});		
			});
			return menuBar;
		}).setWidth("70px").setFlexGrow(0);

		tableContent.addAndExpand(locationTagGrid);
		
		HorizontalLayout buttonWrapper = new HorizontalLayout();
		buttonWrapper.addClassNames("padding-top-bottom-20px");
		Button addNewTagButton = new Button("Add Location Tag");

		locationTagDialog.addConfirmListener(eventListener -> {
			
			LocationTag locationTag = locationTagDialog.getUpdatedLocationTag();
			if (locationTag != null) {

				locationTagGrid.getListDataView().addItem(locationTag);
				ldp.refreshItem(locationTag);
			}
		});	
		
		addNewTagButton.addClickListener(e -> {
			locationTagDialog.clearForm(false);
			locationTagDialog.open();
		});
		buttonWrapper.add(addNewTagButton);
		
		tableContent.add(buttonWrapper);
		
		tableContent.add(new Hr());
		
		HorizontalLayout customerTagWrapper = new HorizontalLayout();
		customerTagWrapper.setSpacing(true);
		customerTagWrapper.setPadding(false);
		customerTagWrapper.addClassName("padding-top-medium");
		Label customerTagLabel = new Label("Customer Tags");
		customerTagLabel.addClassName(CssClassNamesConstants.PROFILE_DETAILS_LABEL_WRAPPER);
		customerTagWrapper.add(customerTagLabel);
		
		
		tableContent.add(customerTagWrapper);
		customerTagGrid = new Grid<>(CustomerTag.class, false);
		customerTagGrid.addColumn("customerTagName").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		customerTagGrid.addColumn("customerTagDescription").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		customerTagGrid.addColumn(locationTags -> {
			Hibernate.initialize(locationTags.getLocationTagSet());
			Set<String> locationTagsString = locationTags.getLocationTagSet().stream().map(LocationTag::getLocationTagName).collect(Collectors.toSet());
			return String.join(", ", locationTagsString).toLowerCase();
		}).setTextAlign(ColumnTextAlign.START).setHeader("Location Tags");
		

		customerTagGrid.addComponentColumn(currentCustomerTag -> {

			MenuBar menuBar = new MenuBar();
			menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON);
			MenuItem menuItem = menuBar.addItem(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
			menuItem.getElement().setAttribute("aria-label", "More options");
			SubMenu subMenu = menuItem.getSubMenu();
			subMenu.addItem("Edit Customer Tag", e -> {
				populateDataAndOpenDialog(currentCustomerTag, customerTagFormDialog);
			});
			
			subMenu.addItem("Delete Customer Tag", e -> {
				ConfirmDialog confirmDialog = new ConfirmDialog();
				confirmDialog.setCancelable(true);
				confirmDialog.setText("This tag will be permanently deleted. Do you want to proceed?");
				confirmDialog.open();
				
				confirmDialog.addConfirmListener(event -> {
					try {

						customerTagService.delete(currentCustomerTag.getId());
						customerTagGrid.getListDataView().removeItem(currentCustomerTag);
						customerTagGrid.getListDataView().refreshAll();
						Notification.show("Customer tag successfully deleted.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					} catch (Exception exception) {
						System.out.println(exception.getMessage());
						Notification.show("Unable to delete Customer Tag because it is still being used in the Sizes.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
						
					}
				});		
			});

			return menuBar;
		}).setWidth("70px").setFlexGrow(0);
		
		
		
		
		customerTagGrid.setAllRowsVisible(true);

		customerTagLdp = DataProvider.ofCollection(customerTagService.listAll(Sort.by("id")));

		customerTagGrid.setItems(customerTagLdp);
		customerTagGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		customerTagGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		customerTagGrid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
		customerTagGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
		
		
		
		tableContent.addAndExpand(customerTagGrid);
		
		HorizontalLayout customerTagButtonWrapper = new HorizontalLayout();
		customerTagButtonWrapper.setSpacing(true);
		customerTagButtonWrapper.setPadding(false);
		customerTagButtonWrapper.addClassNames("padding-top-bottom-20px");
		
		
		
		customerTagFormDialog.addConfirmListener(eventListener -> {
			
			CustomerTag customerTag = customerTagFormDialog.getUpdatedTag();
			if (customerTag != null) {
				customerTagGrid.getListDataView().addItem(customerTag);
				customerTagLdp.refreshItem(customerTag);
			}
		});	
		Button addNewCustomerTagButton = new Button("Add Customer Tag");
		addNewCustomerTagButton.addClickListener(e -> {
			customerTagFormDialog.clearForm(false);
			customerTagFormDialog.open();
		});
		customerTagButtonWrapper.add(addNewCustomerTagButton);
		
		tableContent.add(customerTagButtonWrapper);
		
	}


	private void populateDataAndOpenDialog(CustomerTag currentCustomerTag, CustomerTagFormDialog formDialog) {
		formDialog.open();
		formDialog.setCurrentCustomerSelectionToBinder(currentCustomerTag);
		
	}
}
