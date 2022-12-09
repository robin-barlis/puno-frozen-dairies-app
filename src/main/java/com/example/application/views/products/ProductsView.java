package com.example.application.views.products;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.entity.products.Product;
import com.example.application.data.service.products.CategoryService;
import com.example.application.data.service.products.ProductService;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.example.application.views.products.components.ProductsViewCard;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Products")
@Route(value = "products", layout = MainLayout.class)
@PermitAll
public class ProductsView extends AbstractPfdiView implements HasComponents, HasStyle {

	private static final long serialVersionUID = -6210105239749320428L;
	private OrderedList imageContainer;
	private Button addProductsButton;
	private List<Product> productList;
	private CategoryService categoryService;

	@Autowired
	public ProductsView(ProductService productService, CategoryService categoryService) {
		super("products-view", "Products");
		this.productList = productService.listAll(Sort.unsorted());
		this.categoryService = categoryService;
	}

	@Override
	protected void addChildrenToContentHeaderContainer(VerticalLayout contentHeaderContainer) {
		HorizontalLayout headerContainer = new HorizontalLayout();
		headerContainer.setWidthFull();

		FlexLayout headerNameWrapper = new FlexLayout();
		headerNameWrapper.setFlexDirection(FlexDirection.ROW);
		headerNameWrapper.setJustifyContentMode(JustifyContentMode.START);
		headerNameWrapper.setAlignItems(Alignment.CENTER);
		H1 header = new H1("Product List");
		header.addClassNames("mb-0", "mt-s", "text-xl");

		//headerNameWrapper.add(tabs);
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		FlexLayout flexWrapper = new FlexLayout();
		flexWrapper.setFlexDirection(FlexDirection.ROW);
		flexWrapper.setJustifyContentMode(JustifyContentMode.END);
		flexWrapper.setClassName("button-layout");

		addProductsButton = new Button("Add New Product");
		addProductsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addProductsButton.setClassName(CssClassNamesConstants.GENERIC_BUTTON_CLASS);

		addProductsButton.addClickListener(e -> {
			UI.getCurrent().navigate(AddNewProductView.class);
		});
		flexWrapper.add(addProductsButton);
		flexWrapper.setWidth("50%");
		

		headerContainer.add(headerNameWrapper, flexWrapper);
		contentHeaderContainer.add(headerContainer);

	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContent) {
		mainContent.addClassName("product-view-grid-container");
		imageContainer = new OrderedList();
		imageContainer.addClassNames("gap-xl", "grid", "list-none", "m-0", "p-0");
		mainContent.add(imageContainer);

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

		for (Product product : productList) {
			List<Integer> categoryIdList = product.getProductPrices().stream().map(e -> e.getCategoryId())
					.collect(Collectors.toList());
			Integer categoryId = categoryIdList.get(0);
			String imageUrl = product.getProductPictureUrl() != null ? product.getProductPictureUrl()
					: "https://images.unsplash.com/photo-1519681393784-d120267933ba?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=750&q=80";
			imageContainer
					.add(new ProductsViewCard(product, categoryService.get(categoryId).orElseGet(null), imageUrl));

		}
	}
	

}