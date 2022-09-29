package com.example.application.views.products;

import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.hibernate.Hibernate;
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
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Manage Tags")
@Route(value = "/product/tags", layout = MainLayout.class)
//@RouteAlias(value = "/", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN" })
@Uses(Icon.class)
public class ManageTagsView extends AbstractPfdiView implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	private Grid<LocationTag> locationTagGrid = new Grid<>(LocationTag.class, false);
	private Grid<CustomerTag> customerTagGrid = new Grid<>(CustomerTag.class, false);

	private final LocationTagService locationTagService;
	private final CustomerTagService customerTagService;
	
	ListDataProvider<LocationTag> ldp = null;
	ListDataProvider<CustomerTag> customerTagLdp = null;

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
		
		
		tableContent.add(headingWrapper);
		locationTagGrid = new Grid<>(LocationTag.class, false);
		locationTagGrid.addColumn("locationTagName").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		locationTagGrid.addColumn("locationTagDescription").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		locationTagGrid.setAllRowsVisible(true);

		ldp = DataProvider.ofCollection(locationTagService.listAll(Sort.by("id")));

		locationTagGrid.setItems(ldp);
		locationTagGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		locationTagGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		locationTagGrid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);

		tableContent.addAndExpand(locationTagGrid);
		
		HorizontalLayout buttonWrapper = new HorizontalLayout();
		buttonWrapper.setSpacing(true);
		buttonWrapper.setPadding(false);
		Button addNewTagButton = new Button("Add Location Tag");

		LocationTagFormDialog locationTagDialog =  new LocationTagFormDialog("Add New Tag", locationTagService);
		locationTagDialog.addOpenedChangeListener(eventListener -> {
			
			LocationTag locationTag = locationTagDialog.getUpdatedLocationTag();
			locationTagGrid.getListDataView().addItem(locationTag);

			ldp.refreshItem(locationTag);
			locationTagGrid.getListDataView().refreshAll();
		});	
		
		addNewTagButton.addClickListener(e -> {
			locationTagDialog.open();
		});
		buttonWrapper.add(addNewTagButton);
		
		tableContent.add(buttonWrapper);
		
		tableContent.add(new Hr());
		
		HorizontalLayout customerTagWrapper = new HorizontalLayout();
		customerTagWrapper.setSpacing(true);
		customerTagWrapper.setPadding(false);
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
		
		
		CustomerTagFormDialog customerTagFormDialog = new CustomerTagFormDialog("Add New Customer Tag", customerTagService, locationTagService);
		
		customerTagFormDialog.addOpenedChangeListener(eventListener -> {
			
			CustomerTag customerTag = customerTagFormDialog.getUpdatedTag();
			if (customerTag != null) {
				customerTagGrid.getListDataView().addItem(customerTag);
				customerTagLdp.refreshItem(customerTag);
				customerTagGrid.getListDataView().refreshAll();
			}
		});	
		Button addNewCustomerTagButton = new Button("Add Customer Tag");
		addNewCustomerTagButton.addClickListener(e -> customerTagFormDialog.open());
		customerTagButtonWrapper.add(addNewCustomerTagButton);
		
		tableContent.add(customerTagButtonWrapper);
		
	}
}
