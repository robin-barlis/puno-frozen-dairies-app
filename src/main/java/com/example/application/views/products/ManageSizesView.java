package com.example.application.views.products;

import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.entity.products.CustomerTag;
import com.example.application.data.entity.products.Size;
import com.example.application.data.service.products.CustomerTagService;
import com.example.application.data.service.products.SizesService;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.example.application.views.products.components.SizeFormDialog;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Manage Sizes")
@Route(value = "products/sizes", layout = MainLayout.class)
@PermitAll
public class ManageSizesView extends AbstractPfdiView implements HasComponents, HasStyle {
	
	private static final long serialVersionUID = -6210105239749320428L;
	
	private SizesService sizesService;
	private CustomerTagService customerTagService;
	private Grid<Size> sizesGrid = new Grid<>(Size.class, false);


	ListDataProvider<Size> ldp = null;
	
	@Autowired
	public ManageSizesView(SizesService sizesService, CustomerTagService customerTagService) {
		super("manage-sizes", "Manage Sizes");
		
		this.sizesService = sizesService;
		this.customerTagService = customerTagService;
		
		VerticalLayout content = new VerticalLayout();
		createMainContent(content);
		add(content);

	}
	
	private void createMainContent(VerticalLayout tableContent) {
		HorizontalLayout sizeWrapper = new HorizontalLayout();
		sizeWrapper.setSpacing(true);
		sizeWrapper.setPadding(false);
		Label customerTagLabel = new Label("Manage Sizes");
		customerTagLabel.addClassName(CssClassNamesConstants.PROFILE_DETAILS_LABEL_WRAPPER);
		sizeWrapper.add(customerTagLabel);

		SizeFormDialog sizeFormDialog = new SizeFormDialog("Add New Size", customerTagService, sizesService);
		
		tableContent.add(sizeWrapper);
		sizesGrid = new Grid<>(Size.class, false);
		sizesGrid.addColumn("sizeName").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		sizesGrid.addColumn("sizeDescription").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		sizesGrid.addColumn("sizeCategory").setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Size Type");
		sizesGrid.addColumn(sizeTag -> {
			Hibernate.initialize(sizeTag.getCustomerTagSet());
			Set<String> locationTagsString = sizeTag.getCustomerTagSet().stream().map(CustomerTag::getCustomerTagName).collect(Collectors.toSet());
			return String.join(", ", locationTagsString).toLowerCase();
		}).setTextAlign(ColumnTextAlign.START).setHeader("Customer Tag");
		sizesGrid.addComponentColumn(currentSize -> {

			MenuBar menuBar = new MenuBar();
			menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON);
			MenuItem menuItem = menuBar.addItem(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
			menuItem.getElement().setAttribute("aria-label", "More options");
			SubMenu subMenu = menuItem.getSubMenu();
			subMenu.addItem("Edit Size", e -> {
				populateDataAndOpenDialog(currentSize, sizeFormDialog);
			});
			
			subMenu.addItem("Delete ", e -> {
				ConfirmDialog confirmDialog = new ConfirmDialog();
				confirmDialog.setCancelable(true);
				confirmDialog.setText("This tag will be permanently deleted. Do you want to proceed?");
				confirmDialog.open();
				
				confirmDialog.addConfirmListener(event -> {
					try {

						sizesService.delete(currentSize.getId());
						sizesGrid.getListDataView().removeItem(currentSize);
						sizesGrid.getListDataView().refreshAll();
						Notification.show("Customer tag successfully deleted.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					} catch (Exception exception) {
						System.out.println(exception.getMessage());
						Notification.show("Unable to delete Customer Tag because it is still being used in the Sizes.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
						
					}
				});		
			});

			return menuBar;
		}).setWidth("70px").setFlexGrow(0);

		ldp = DataProvider.ofCollection(sizesService.listAll(Sort.by("id")));

		sizesGrid.setItems(ldp);
		sizesGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		sizesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		sizesGrid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
		sizesGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
		sizesGrid.setAllRowsVisible(true);
		sizesGrid.setHeightFull();
		sizesGrid.setMaxHeight("1eh");
		tableContent.add(sizesGrid);
		
		HorizontalLayout sizeButtonWrapper = new HorizontalLayout();
		sizeButtonWrapper.setSpacing(true);
		sizeButtonWrapper.setPadding(false);
		sizeButtonWrapper.addClassNames("padding-top-bottom-20px");
		
		
		
		sizeFormDialog.addConfirmListener(eventListener -> {
			
			Size sizes = sizeFormDialog.getUpdateSize();
			if (sizes != null) {
				sizesGrid.getListDataView().addItem(sizes);
				ldp.refreshItem(sizes);
				sizesGrid.getListDataView().refreshAll();
			}
		});	
		Button addNewSizeButton = new Button("Add Size");
		addNewSizeButton.addThemeVariants(ButtonVariant.LUMO_LARGE);
		addNewSizeButton.addClickListener(e -> {
			sizeFormDialog.setCurrentSelectionToBinder(null);
			sizeFormDialog.open();
		});
		sizeButtonWrapper.add(addNewSizeButton);
		
		tableContent.add(sizeButtonWrapper);
		
	}

	public void populateDataAndOpenDialog(Size currentSize, SizeFormDialog sizeFormDialog) {
		sizeFormDialog.open();
		sizeFormDialog.setCurrentSelectionToBinder(currentSize);
	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContent) {
	

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		
	}
}