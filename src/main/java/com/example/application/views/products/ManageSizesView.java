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
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
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
		super("manage-sizes", "Product Management > Manage Sizes");
		
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
		Label customerTagLabel = new Label("Product Management > Manage Sizes");
		customerTagLabel.addClassName(CssClassNamesConstants.PROFILE_DETAILS_LABEL_WRAPPER);
		sizeWrapper.add(customerTagLabel);
		
		
		tableContent.add(sizeWrapper);
		sizesGrid = new Grid<>(Size.class, false);
		sizesGrid.addColumn("sizeName").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		sizesGrid.addColumn("sizeDescription").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		sizesGrid.addColumn(sizeTag -> {
			Hibernate.initialize(sizeTag.getCustomerTagSet());
			Set<String> locationTagsString = sizeTag.getCustomerTagSet().stream().map(CustomerTag::getCustomerTagName).collect(Collectors.toSet());
			return String.join(", ", locationTagsString).toLowerCase();
		}).setTextAlign(ColumnTextAlign.START).setHeader("Sizes");
		sizesGrid.setAllRowsVisible(true);

		ldp = DataProvider.ofCollection(sizesService.listAll(Sort.by("id")));

		sizesGrid.setItems(ldp);
		sizesGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		sizesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		sizesGrid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
		sizesGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
		tableContent.addAndExpand(sizesGrid);
		
		HorizontalLayout sizeButtonWrapper = new HorizontalLayout();
		sizeButtonWrapper.setSpacing(true);
		sizeButtonWrapper.setPadding(false);
		
		
		SizeFormDialog sizeFormDialog = new SizeFormDialog("Add New Size", customerTagService, sizesService);
		
		sizeFormDialog.addOpenedChangeListener(eventListener -> {
			
			Size sizes = sizeFormDialog.getUpdateSize();
			if (sizes != null) {
				sizesGrid.getListDataView().addItem(sizes);
				ldp.refreshItem(sizes);
				sizesGrid.getListDataView().refreshAll();
			}
		});	
		Button addNewSizeButton = new Button("Add Size");
		addNewSizeButton.addClickListener(e -> sizeFormDialog.open());
		sizeButtonWrapper.add(addNewSizeButton);
		
		tableContent.add(sizeButtonWrapper);
		
	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContent) {
	

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		
	}
}