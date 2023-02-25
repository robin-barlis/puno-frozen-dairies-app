package com.example.application.views.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import com.example.application.utils.PfdiUtil;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;

public class ItemOrderCategorySubView extends VerticalLayout {

	private static final long serialVersionUID = -5585110820503078437L;

	private Customer customer;

	private List<Items> itemsList = new ArrayList<>();

	private Set<OrderItems> orderItems;

	private BigDecimal currentTotalPrice = BigDecimal.valueOf(0);

	private H1 totalAmount = new H1();
	private Map<String, OrderItems> orderItemMap = null;

	public ItemOrderCategorySubView(List<Category> categories, Customer customer,
			Map<String, List<Product>> productCategoryMap) {
		this.customer = customer;

		totalAmount.setText("Total Amount: " + currentTotalPrice);
		totalAmount.addClassNames("mb-1", "mt-s", "text-l", "span-order-flavor-column-header");
		totalAmount.setWidthFull();
		categories.forEach(category -> {

			List<Product> productsPerCategory = productCategoryMap.get(category.getCategoryName());
			if (productsPerCategory != null && !productsPerCategory.isEmpty()) {
				System.out.println("Category Name: " + category.getCategoryName());
				createContent(category, productsPerCategory);
			}

		});

	}

	private void createContent(Category category, List<Product> products) {

		VerticalLayout layout = new VerticalLayout();
		layout.setClassName("item-order-category-subview");
		H3 categoryHeader = new H3(category.getCategoryName());
		categoryHeader.addClassNames("mb-0", "mt-s", "text-m");
		layout.add(categoryHeader);

		HorizontalLayout headerWrapper = new HorizontalLayout();
		headerWrapper.setWidthFull();
		headerWrapper.setBoxSizing(BoxSizing.BORDER_BOX);
		headerWrapper.setPadding(false);

		category.getSizeSet();

		boolean headerAlreadyRendered = false;
		for (Product product : products) {

			CustomerTag customerTag = customer.getCustomerTagId();

			Map<String, ItemStock> itemStock = PfdiUtil.createSizeMap(product);
			Map<Integer, List<ProductPrice>> productPricePerCustomer = PfdiUtil.createProductPricePerSizeId(product, customerTag);
			List<Size> sizes = category.getSizeSet().stream().collect(Collectors.toList());

			PfdiUtil.sort(category, sizes);
			if (!headerAlreadyRendered) {
				VerticalLayout flavorColumnHeaderWrapper = new VerticalLayout();
				flavorColumnHeaderWrapper.addClassName("span-order-flavor-column-header");

				Span flavorColumnHeader = new Span();
				flavorColumnHeader.setText(category.getCategoryType());
				flavorColumnHeader.addClassNames("mb-1", "mt-s", "text-s");
				flavorColumnHeaderWrapper.add(flavorColumnHeader);
				headerWrapper.add(flavorColumnHeaderWrapper);

				sizes.forEach(size -> {
					VerticalLayout sizeColumnHeaderWrapper = new VerticalLayout();
					sizeColumnHeaderWrapper.addClassName("span-order-size-column-header");

					Span sizeColumnHeader = new Span();
					sizeColumnHeader.setText(size.getSizeName());
					sizeColumnHeader.addClassNames("mb-1", "mt-s", "text-s", "order-column");
					sizeColumnHeaderWrapper.add(sizeColumnHeader);
					headerWrapper.add(sizeColumnHeaderWrapper);

				});

				layout.add(headerWrapper);
				layout.add(new Hr());
				headerAlreadyRendered = true;
			}

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

			sizes.forEach(size -> {

				Set<String> customerTagNameSet = size.getCustomerTagSet().stream().map(CustomerTag::getCustomerTagName)
						.collect(Collectors.toSet());

				List<ProductPrice> prices = productPricePerCustomer.get(size.getId());
				if (customerTagNameSet.contains(customerTag.getCustomerTagName()) && Objects.nonNull(prices)) {

					Optional<ProductPrice> productPriceOpt = prices.stream().filter(e -> {
						return e.getCustomerTagId() == customerTag.getId()
								&& customer.getLocationTagId().getId() == e.getLocationTagId();
					}).findFirst();

					if (!productPriceOpt.isEmpty()) {
						ProductPrice productPrice = productPriceOpt.get();
						if (productPrice != null) {
							IntegerField quantityField = new IntegerField();

							int quantity = 0;

							ItemStock stock = itemStock.get(size.getSizeName());
							if (stock != null) {
								quantity = itemStock.get(size.getSizeName()).getAvailableStock();
							}

							quantityField.setMax(quantity);
							quantityField.setId("id-" + product.getProductShortCode() + "-" + size.getSizeName());
							quantityField.addClassName("span-order-size-column");
							quantityField.setHelperText("Stock: " + quantity);
							quantityField.setValue(0);
							quantityField.setMin(0);
							System.out.println("id-" + product.getProductShortCode() + "-" + size.getSizeName());
							quantityField.addValueChangeListener(e -> {

								BigDecimal oldAmount = productPrice.getTransferPrice()
										.multiply(BigDecimal.valueOf(e.getOldValue() != null ? e.getOldValue() : 0));
								BigDecimal newAmount = productPrice.getTransferPrice()
										.multiply(BigDecimal.valueOf(e.getValue() != null ? e.getValue() : 0));

								currentTotalPrice = currentTotalPrice.subtract(oldAmount);
								currentTotalPrice = currentTotalPrice.add(newAmount);
								System.out.println("Old Amount : " + oldAmount);
								System.out.println("New Amount : " + newAmount);
								System.out.println("Current Total Amount : " + currentTotalPrice);

								totalAmount
										.setText("Total Amount : " + PfdiUtil.getFormatter().format(currentTotalPrice));
								add(totalAmount);

							});
							contentWrapper.add(quantityField);

							itemsList.add(new Items(quantityField, productPrice.getTransferPrice(), stock));
						}

					}

				} else {
					IntegerField quantityField = new IntegerField();
					quantityField.setMax(0);
					quantityField.addClassName("span-order-size-column");
					quantityField.setHelperText("Stock: N/A");
					quantityField.setValue(0);
					quantityField.setMin(0);
					quantityField.setEnabled(false);
					contentWrapper.add(quantityField);
				}
			});

			layout.add(contentWrapper);

		}

		add(layout);

	}

	public Set<OrderItems> createOrderItems(AppUser user, LocalDateTime localDateTime, Order order) {
		return itemsList.stream().map(items -> {
			if (items.getNumberField().getValue() > 0) {

				int quantity = items.getNumberField().getValue();

				OrderItems orderItem = new OrderItems();

				orderItem.setQuantity(quantity);

				ItemStock stock = items.getItemStock();
				int availableStock = stock.getAvailableStock();
				int adjustment = availableStock - quantity;
				stock.setAvailableStock(adjustment);
				orderItem.setItemInventory(stock);
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

	public BigDecimal getTotalAmount() {
		return currentTotalPrice;
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

	public void setOrderItems(Set<OrderItems> orderItems) {
		this.orderItems = orderItems;

		setValues();
	}

	public Set<OrderItems> updateOrderItems(AppUser user) {
		itemsList.forEach(itemField -> {
			IntegerField field = itemField.getNumberField();
			String itemFieldId = field.getId().orElse(null);
			if (itemFieldId != null) {
				Integer quantity = field.getValue();
				OrderItems orderItem = orderItemMap.get(itemFieldId);
				if (orderItem != null) {
					Integer oldValue = orderItem.getQuantity();

					if (!quantity.equals(oldValue)) {
						orderItem.setQuantity(quantity);
						ItemStock inventory = orderItem.getItemInventory();

						Integer availableStock = inventory.getAvailableStock();

						Integer currentStock = availableStock + oldValue - quantity;

						inventory.setAvailableStock(currentStock);
						orderItem.setUpdatedDate(LocalDateTime.now());
						orderItem.setUpdatedBy(user);
					}
				}

			}
		});
		return orderItemMap.values().stream().collect(Collectors.toSet());
	}

	private void setValues() {
		orderItemMap = orderItems.stream().collect(Collectors.toMap(e -> {

			String productShortCode = e.getItemInventory().getProduct().getProductShortCode();
			String sizeName = e.getItemInventory().getSize().getSizeName();
			return "id-" + productShortCode + "-" + sizeName;
		}, Function.identity()));

		itemsList.forEach(itemField -> {

			IntegerField field = itemField.getNumberField();
			String fieldId = field.getId().orElse(null);

			if (fieldId != null) {
				System.out.println("Field ID: " + fieldId);
				OrderItems orderItem = orderItemMap.get(fieldId);
				Integer value = orderItem != null ? orderItem.getQuantity() : 0;
				field.setValue(value);
			}
		});

	}

}
