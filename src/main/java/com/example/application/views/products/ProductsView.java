package com.example.application.views.products;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.application.data.Categories;
import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Product;
import com.example.application.data.service.products.CategoryService;
import com.example.application.data.service.products.ProductService;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.example.application.views.products.components.FlavorSortingDialog;
import com.example.application.views.products.components.ProductsViewCard;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
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
	private OrderedList regularFlavorList;
	private OrderedList specialFlavorList;
	private OrderedList sherbetFlavorList;
	private OrderedList coneList;
	private OrderedList otherList;

	private Button addProductsButton;
	private Map<String, List<Product>> productList;
	private CategoryService categoryService;
	private ProductService productService;

	@Autowired
	public ProductsView(ProductService productService, CategoryService categoryService) {
		super("products-view", "Products");
		this.productList = productService.listAllByCategory();
		this.categoryService = categoryService;
		this.productService = productService;
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

		// headerNameWrapper.add(tabs);
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
		HorizontalLayout optionsContainer = new HorizontalLayout();
		optionsContainer.setJustifyContentMode(JustifyContentMode.END);
		optionsContainer.setPadding(false);
		optionsContainer.setWidth("100%");

		MenuBar options = new MenuBar();
		options.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON, MenuBarVariant.LUMO_LARGE);
		
		Icon cogIcon = new Icon(VaadinIcon.COG);
		cogIcon.getStyle().set("font-size", "18px");
		
		MenuItem optionsMenu = options.addItem(cogIcon);
		
		SubMenu optionsMenuSubItems = optionsMenu.getSubMenu();

		MenuItem flavorSorting = optionsMenuSubItems.addItem("Manage Flavor Sorting");
		flavorSorting.addClickListener(e -> {
			List<Product> flavorsList = productList.get(Categories.Flavors.name());
			new FlavorSortingDialog(flavorsList, productService).open();
		});

		optionsContainer.add(options);

		mainContent.addClassName("product-view-grid-container");

		VerticalLayout flavorListHeaderContainer = createHeaderForLayout("Regular Flavor List");
		
		VerticalLayout specialListHeaderContainer = createHeaderForLayout("Special Flavor List");
		
		VerticalLayout sherbetListHeaderContainer = createHeaderForLayout("Sherbet List");

		VerticalLayout coneListHeaderContainer = createHeaderForLayout("Cones List");

		VerticalLayout otherListHeaderContainer = createHeaderForLayout("Other Products List");

		regularFlavorList = new OrderedList();
		regularFlavorList.addClassNames("gap-xl", "grid", "list-none", "m-0", "p-0");
		
		specialFlavorList = new OrderedList();
		specialFlavorList.addClassNames("gap-xl", "grid", "list-none", "m-0", "p-0");

		
		sherbetFlavorList = new OrderedList();
		sherbetFlavorList.addClassNames("gap-xl", "grid", "list-none", "m-0", "p-0");


		coneList = new OrderedList();
		coneList.addClassNames("gap-xl", "grid", "list-none", "m-0", "p-0");

		otherList = new OrderedList();
		otherList.addClassNames("gap-xl", "grid", "list-none", "m-0", "p-0");

		mainContent.add(optionsContainer, flavorListHeaderContainer, regularFlavorList, specialListHeaderContainer, specialFlavorList, sherbetListHeaderContainer, sherbetFlavorList, coneListHeaderContainer, coneList,
				otherListHeaderContainer, otherList);

	}

	private VerticalLayout createHeaderForLayout(String headerTitle) {
		
		VerticalLayout container = new VerticalLayout();
		container.setPadding(true);
		HorizontalLayout flavorListHeaderContainer = new HorizontalLayout();
		flavorListHeaderContainer.setJustifyContentMode(JustifyContentMode.START);
		flavorListHeaderContainer.setPadding(true);;
		flavorListHeaderContainer.setWidth("100%");
		H2 flavorListHeader = new H2(headerTitle);
		flavorListHeader.addClassNames("mb-0", "mt-s", "text-xl");

		flavorListHeaderContainer.add(flavorListHeader);
		container.add(flavorListHeaderContainer, new Hr());
		return container;
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

		for (Entry<String, List<Product>> entrySet : productList.entrySet()) {

			String key = entrySet.getKey();
			List<Product> products = entrySet.getValue();

			for (Product product : products) {
				List<Integer> categoryIdList = product.getProductPrices().stream().map(e -> e.getCategoryId())
						.collect(Collectors.toList());

				Integer categoryId = !categoryIdList.isEmpty() ? categoryIdList.get(0) : null;

				Category category = null;

				if (categoryId != null) {
					category = categoryService.get(categoryId).orElseGet(null);
				}
				String imageUrl = product.getProductPictureUrl() != null ? product.getProductPictureUrl()
						: "https://res.cloudinary.com/dw2qyhgar/image/upload/v1676078906/170043505_10158971498776278_8359436008848051948_n_jknb5u.jpg";

				ProductsViewCard card = new ProductsViewCard(product, category, imageUrl);

				if (Categories.Flavors.name().equalsIgnoreCase(key)) {
					
					if ("Regular Ice Cream".equals(product.getCategory().getCategoryName())) {
						regularFlavorList.add(card);			
					} else if ("Sherbet".equals(product.getCategory().getCategoryName())) {
						sherbetFlavorList.add(card);
					} else if ("Special/Premium Ice Cream".equals(product.getCategory().getCategoryName())) {
						specialFlavorList.add(card);
					}
				} else if (Categories.Cones.name().equalsIgnoreCase(key)) {
					coneList.add(card);
				} else {
					otherList.add(card);
				}
			}

		}
	}

}