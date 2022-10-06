package com.example.application.views.products;

import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Size;
import com.example.application.data.service.products.CategoryService;
import com.example.application.data.service.products.SizesService;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.example.application.views.products.components.CategoryFormDialog;
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

@PageTitle("Manage Categories")
@Route(value = "productCategories", layout = MainLayout.class)
@PermitAll
public class ManageCategoriesView extends AbstractPfdiView implements HasComponents, HasStyle {
	
	private static final long serialVersionUID = -6210105239749320428L;
	
	private CategoryService categoryService;
	private SizesService sizesService;
	
	private Grid<Category> categoryGrid = new Grid<>(Category.class, false);
	ListDataProvider<Category> ldp = null;
	
	@Autowired
	public ManageCategoriesView(CategoryService categoryService, SizesService sizesService) {
		super("manage-categories", "Product Management > Manage Categories");
		this.categoryService = categoryService;
		this.sizesService = sizesService;
		
		VerticalLayout content = new VerticalLayout();
		createMainContent(content);
		add(content);
	}

	private void createMainContent(VerticalLayout tableContent) {
		HorizontalLayout sizeWrapper = new HorizontalLayout();
		sizeWrapper.setSpacing(true);
		sizeWrapper.setPadding(false);
		Label customerTagLabel = new Label("Product Management > Manage Categories");
		customerTagLabel.addClassName(CssClassNamesConstants.PROFILE_DETAILS_LABEL_WRAPPER);
		sizeWrapper.add(customerTagLabel);
		
		
		tableContent.add(sizeWrapper);
		categoryGrid = new Grid<>(Category.class, false);
		categoryGrid.addColumn("categoryName").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		categoryGrid.addColumn("categoryDescription").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		categoryGrid.addColumn(category -> {
			Hibernate.initialize(category.getSizeSet());
			Set<String> locationTagsString = category.getSizeSet().stream().map(Size::getSizeName).collect(Collectors.toSet());
			return String.join(", ", locationTagsString).toLowerCase();
		}).setTextAlign(ColumnTextAlign.START).setHeader("Sizes");
		categoryGrid.setAllRowsVisible(true);

		ldp = DataProvider.ofCollection(categoryService.listAll(Sort.by("id")));

		categoryGrid.setItems(ldp);
		categoryGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		categoryGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		categoryGrid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
		categoryGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
		tableContent.addAndExpand(categoryGrid);
		
		HorizontalLayout categoryButtonWrapper = new HorizontalLayout();
		categoryButtonWrapper.setPadding(false);
		categoryButtonWrapper.addClassNames("padding-top-bottom-20px");
		
		
		CategoryFormDialog categoryFormDialog = new CategoryFormDialog("Add New Size",categoryService, sizesService );
		
		categoryFormDialog.addOpenedChangeListener(eventListener -> {
			
			Category category = categoryFormDialog.getUpdatedCategory();
			if (category != null) {
				categoryGrid.getListDataView().addItem(category);
				ldp.refreshItem(category);
				categoryGrid.getListDataView().refreshAll();
			}
		});	
		Button addNewCategoryButton = new Button("Add Category");
		addNewCategoryButton.addClickListener(e -> categoryFormDialog.open());
		categoryButtonWrapper.add(addNewCategoryButton);
		
		tableContent.add(categoryButtonWrapper);
		
		
	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContent) {
	

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		
	}
}