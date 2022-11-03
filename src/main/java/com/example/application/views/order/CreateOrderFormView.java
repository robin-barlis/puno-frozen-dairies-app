package com.example.application.views.order;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.orders.OrderItems;
import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Product;
import com.example.application.data.service.customers.CustomerService;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.data.service.products.CategoryService;
import com.example.application.data.service.products.ProductService;
import com.example.application.data.service.stock.ItemStockService;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.products.ProductsView;
import com.example.application.views.products.components.SizePricingSubView;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;

@PageTitle("Add New Product")
@Route(value = "order/createOrder", layout = MainLayout.class)
@PermitAll
public class CreateOrderFormView extends AbstractPfdiView implements HasComponents, HasStyle {

	private static final long serialVersionUID = -6210105239749320428L;

	private DateTimePicker orderCreationDate;
	private Select<Customer> storeName;
	private Button cancelButton;
	//private Button saveAsDraftButton;
	private Button publishButton;
	private Button saveAsDraftButton;
	private CategoryService categoryService;
	private ProductService productService;
	private HashSet<SizePricingSubView> pricingSubViewSet = new HashSet<>();

	private ItemStockService itemStockService;
	private AuthenticatedUser authenticatedUser;
	private CustomerService customerService;
	private TextField ownerName;
	private OrdersService orderService;
	private Set<ItemOrderCategorySubView> itemOrderCategorySubViewSet = new HashSet<>();
	

	@Autowired
	public CreateOrderFormView(AuthenticatedUser authenticatedUser, OrdersService stockOrderService, CustomerService customerService, ItemStockService itemStockService, ProductService prodcuctService, CategoryService categoryService, AccessAnnotationChecker accessChecker) {
		super("add-new-product", "Crete Stock Order");
		//addClassNames("products-view");
		this.customerService = customerService;
		this.itemStockService = itemStockService;
		this.authenticatedUser = authenticatedUser;
		this.productService = prodcuctService;
		this.categoryService = categoryService;
		this.orderService = stockOrderService;
		VerticalLayout content = new VerticalLayout();
		createMainContent(content);
		add(content);

	}

	private void createMainContent(VerticalLayout content) {
		
		VerticalLayout headerWrapper = new VerticalLayout();
		orderCreationDate = new DateTimePicker("Order Creation Date & Time");
		orderCreationDate.setWidth("50%");
		orderCreationDate.setRequiredIndicatorVisible(true);
		orderCreationDate.setValue(LocalDateTime.now());
		
		HorizontalLayout customerInfoWrapper = new HorizontalLayout();
		customerInfoWrapper.setWidthFull();
		
		ownerName = new TextField();
		ownerName = new TextField("Owner Name");
		ownerName.setReadOnly(true);

		ownerName.setWidth("50%");
		

		VerticalLayout formContent = new VerticalLayout();
		
		List<Customer> availableCustomers = customerService.listAll(Sort.unsorted());
		storeName = new Select<>();
		storeName.setLabel("Outlet Name");
		storeName.setWidth("50%");
		storeName.setEmptySelectionAllowed(false);
		storeName.setItems(availableCustomers);
		storeName.setItemLabelGenerator(e -> {
			return e.getStoreName();
		});
		storeName.getStyle().set("padding-bottom", "20px");
		storeName.setRequiredIndicatorVisible(true);
		storeName.setEmptySelectionAllowed(false);
		storeName.setPlaceholder("Select Position");
		storeName.addValueChangeListener(e -> {
			ownerName.setValue(e.getValue().getOwnerName());
			
			itemOrderCategorySubViewSet.forEach(item -> {
				formContent.remove(item);
			});
			createOrderContent(formContent, e.getValue());
		});
		
		customerInfoWrapper.add(storeName, ownerName);
		
		
		headerWrapper.add(orderCreationDate, customerInfoWrapper);

		cancelButton = new Button("Cancel");
		
		cancelButton.addClickListener(e -> {
//			newAddSizeContainer.removeAll();
//			category.setValue(null);
//			productName.setValue(null);
			UI.getCurrent().navigate(StockOrderView.class);
			
		});
		
		publishButton = new Button("Next");
		publishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		publishButton.addClickListener(e -> {

			Order order = createNewOrder();
			order = orderService.update(order);
			//TODO update inventory
			Notification.show("Successfully created order.");
			UI.getCurrent().navigate(StockOrderView.class);
		});
		

		content.add(headerWrapper);
		content.add(new Hr());
		content.add(formContent);
		
		
		
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setAlignItems(Alignment.END);
		buttonsLayout.setJustifyContentMode(JustifyContentMode.END);
		buttonsLayout.setWidthFull();
		buttonsLayout.setVerticalComponentAlignment(Alignment.END);
		
		
		buttonsLayout.add(cancelButton);
		buttonsLayout.add(publishButton);
		
		content.add(buttonsLayout);


	}

	private Order createNewOrder() {
		Order order = new Order();
		
		
		AppUser user = authenticatedUser.get().get();
		order.setCreatedByUser(user);
		order.setCreationDate(orderCreationDate.getValue());
		order.setCustomer(storeName.getValue());
		order.setStatus("For Checking");
		order.setUpdatedByUser(user);
		order.setUpdatedDate(orderCreationDate.getValue());
		
		Set<OrderItems> orderItemSet = new HashSet<>();
		itemOrderCategorySubViewSet.forEach(itemOrderSubView -> {
			orderItemSet.addAll(itemOrderSubView.createOrderItems(user, orderCreationDate.getValue(), order));
		});
		
		order.setOrderItems(orderItemSet);
		return order;
	}

	private void createOrderContent(VerticalLayout content, Customer customer) {
		
		
		List<Product> products = productService.listAll(Sort.by(Sort.Direction.DESC, "category_id"));
		
		
		List<Category> categories = categoryService.listAll(Sort.by(Sort.Direction.ASC, "id"));
		Map<String, List<Product>> productCategoryMap = products.stream().collect(Collectors.groupingBy(e -> {
			return e.getCategory().getCategoryName();	
		}));
		
		categories.forEach(category -> {
			
			List<Product> productsPerCategory = productCategoryMap.get(category.getCategoryName());
			
			if (productsPerCategory != null && !productsPerCategory.isEmpty()) {
				ItemOrderCategorySubView itemOrderCategorySubView = new ItemOrderCategorySubView(category, productsPerCategory, customer);
				itemOrderCategorySubViewSet.add(itemOrderCategorySubView);
				content.add(itemOrderCategorySubView);
				
			}
		});
		

		
	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContent) {

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

	}
}