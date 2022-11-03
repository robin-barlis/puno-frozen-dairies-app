package com.example.application.views.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.orders.OrderItems;
import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.CustomerTag;
import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.ProductPrice;
import com.example.application.data.entity.products.Size;
import com.example.application.data.entity.stock.ItemStock;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;

public class ItemOrderCategorySubView extends VerticalLayout {

	private static final long serialVersionUID = -5585110820503078437L;

	private Category category;
	private List<Product> products;
	private Customer customer;

	private List<Items> itemsList;
	public ItemOrderCategorySubView(Category category, List<Product> products, Customer customer) {
		this.category = category;
		this.products = products;
		this.customer = customer;
		createContent();

	}

	private void createContent() {

		VerticalLayout layout = new VerticalLayout();

		H3 categoryHeader = new H3(category.getCategoryName());
		categoryHeader.addClassNames("mb-0", "mt-s", "text-m");
		layout.add(categoryHeader);

		HorizontalLayout headerWrapper = new HorizontalLayout();
		headerWrapper.setWidthFull();
		headerWrapper.setBoxSizing(BoxSizing.BORDER_BOX);
		headerWrapper.setPadding(false);

		category.getSizeSet();
		
		boolean headerAlreadyRendered = false;
		itemsList = new ArrayList<>();
		for (Product product : products) {

			CustomerTag customerTag = customer.getCustomerTagId();
			
			Map<String, ItemStock> itemStock = product.getItemStock().stream().collect(Collectors.toMap(e -> e.getSize().getSizeName(), Function.identity()));
			Map<Integer, List<ProductPrice>> productPricePerCustomer = product.getProductPrices().stream().filter(productPrice -> {
				return productPrice.getCustomerTagId() == customerTag.getId();
			}).collect(Collectors.groupingBy(productPrice -> productPrice.getSizeId()));

			
			if (!headerAlreadyRendered) {
				VerticalLayout flavorColumnHeaderWrapper = new VerticalLayout();
				flavorColumnHeaderWrapper.addClassName("span-order-flavor-column-header");

				Span flavorColumnHeader = new Span();
				flavorColumnHeader.setText(category.getCategoryType());
				flavorColumnHeader.addClassNames("mb-1", "mt-s", "text-s");
				flavorColumnHeaderWrapper.add(flavorColumnHeader);
				headerWrapper.add(flavorColumnHeaderWrapper);


				category.getSizeSet().forEach(size -> {
					VerticalLayout sizeColumnHeaderWrapper = new VerticalLayout();
					sizeColumnHeaderWrapper.addClassName("span-order-size-column-header");

					Set<String> customerTagNameSet = size.getCustomerTagSet().stream().map(CustomerTag::getCustomerTagName)
							.collect(Collectors.toSet());
					if (customerTagNameSet.contains(customerTag.getCustomerTagName()) && productPricePerCustomer.containsKey(size.getId())) {

						Span sizeColumnHeader = new Span();
						sizeColumnHeader.setText(size.getSizeName());
						sizeColumnHeader.addClassNames("mb-1", "mt-s", "text-s");
						sizeColumnHeaderWrapper.add(sizeColumnHeader);
						headerWrapper.add(sizeColumnHeaderWrapper);
					}

				});
				headerAlreadyRendered = true;
			}

			layout.add(headerWrapper);
			layout.add(new Hr());

			HorizontalLayout contentWrapper = new HorizontalLayout();
			contentWrapper.setWidthFull();
			contentWrapper.setBoxSizing(BoxSizing.BORDER_BOX);
			contentWrapper.setPadding(false);

			VerticalLayout flavorColumnWrapper = new VerticalLayout();
			flavorColumnWrapper.addClassName("span-order-flavor-column");

			Span flavorColumnRow = new Span();
			flavorColumnRow.setText(product.getProductName());
			flavorColumnRow.addClassNames("mb-1", "mt-s", "text-s", "span-order-flavor-column");
			flavorColumnWrapper.add(flavorColumnRow);
			contentWrapper.add(flavorColumnWrapper);

			category.getSizeSet().forEach(size -> {

				Set<String> customerTagNameSet = size.getCustomerTagSet().stream().map(CustomerTag::getCustomerTagName)
						.collect(Collectors.toSet());
				
				List<ProductPrice> prices = productPricePerCustomer.get(size.getId());
				if (customerTagNameSet.contains(customerTag.getCustomerTagName()) && Objects.nonNull(prices)) {
					
					ProductPrice productPrice = prices.stream().filter(e-> {
						return e.getCustomerTagId()	== customerTag.getId() && customer.getLocationTagId().getId() == e.getLocationTagId();
					}).findFirst().orElseGet(null);
					IntegerField quantityField = new IntegerField();
					
					int quantity = 0;
					
					ItemStock stock = itemStock.get(size.getSizeName());
					if (stock != null) {
						quantity = itemStock.get(size.getSizeName()).getAvailableStock();
					}

					quantityField.setMax(quantity);
					quantityField.addClassName("span-order-size-column");
					quantityField.setHelperText("Stock: " + quantity);
					quantityField.setHasControls(true);
					quantityField.setValue(0);
					quantityField.setMin(0);
					contentWrapper.add(quantityField);
					
					itemsList.add(new Items(quantityField, productPrice.getTransferPrice(), stock));
					
					
				}
			});

			layout.add(contentWrapper);

		}

		add(layout);

	}
	
	public Set<OrderItems> createOrderItems(AppUser user, LocalDateTime localDateTime, Order order) {
		return itemsList.stream().map(items -> {
			if (items.getNumberField().getValue() > 0) {

				OrderItems orderItem = new OrderItems();
				
				orderItem.setQuantity(items.getNumberField().getValue());
				orderItem.setItemInventory(items.getItemStock());
				orderItem.setProductPrice(items.getPrice());
				orderItem.setUpdatedDate(localDateTime);
				orderItem.setCreatedBy(user);
				orderItem.setCreatedDate(localDateTime);
				orderItem.setUpdatedBy(user);
				orderItem.setOrder(order);
					
				
				return orderItem;
			}
			return null;
		}).filter(Objects::nonNull).collect(Collectors.toSet());	
	}
	
	private class Items {
		
		private IntegerField numberField;
		private BigDecimal price;
		private ItemStock itemStock;
		
		public Items(IntegerField numberField, BigDecimal price, ItemStock itemStock) {
			super();
			this.numberField = numberField;
			this.price = price;
			this.itemStock = itemStock;
		}

		public IntegerField getNumberField() {
			return numberField;
		}

		public BigDecimal getPrice() {
			return price;
		}

		public ItemStock getItemStock() {
			return itemStock;
		}

		
	}


}
