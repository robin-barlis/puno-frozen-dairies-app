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

import com.example.application.IncorrectOrderException;
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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
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
	
	private BigDecimal currentTotalSrp = BigDecimal.valueOf(0);

	private H1 totalAmount = new H1();
	private Map<String, OrderItems> orderItemMap = Maps.newHashMap();;
	private Map<String, IntegerField> itemMap = Maps.newHashMap();


	int productIndex = 0;

	public ItemOrderCategorySubView(List<Category> categories, Customer customer,
			Map<String, List<Product>> productCategoryMap) {
		this.customer = customer;

		System.out.println("Creating Item Order Category Sub View");
		VerticalLayout totalAmountContainer = new VerticalLayout();
		totalAmount.setText("Total Amount: " + PfdiUtil.getFormatter().format(currentTotalPrice));
		totalAmount.addClassNames("mb-1", "mt-s", "text-l");
		totalAmount.setWidthFull();
		totalAmountContainer.add(totalAmount);
		Accordion accordion = new Accordion();
		for (Category category : categories){

			List<Product> productsPerCategory = productCategoryMap.get(category.getCategoryName());
			if (productsPerCategory != null && !productsPerCategory.isEmpty()) {

				H2 categoryHeader = new H2(category.getCategoryName());
				categoryHeader.addClassNames("mb-1", "mt-s", "text-l");
				System.out.println("Category Name: " + category.getCategoryName());
				accordion.add(new AccordionPanel(categoryHeader, createContent(category, productsPerCategory)));
			}

		}
		
		accordion.setWidthFull();
		add(accordion);
		add(totalAmountContainer);

	}

	private VerticalLayout createContent(Category category, List<Product> products) {

		VerticalLayout layout = new VerticalLayout();
		layout.setClassName("item-order-category-subview");

		HorizontalLayout headerWrapper = new HorizontalLayout();
		headerWrapper.setWidthFull();
		headerWrapper.setBoxSizing(BoxSizing.BORDER_BOX);
		headerWrapper.setPadding(false);

		category.getSizeSet();

		boolean headerAlreadyRendered = false;
		for (Product product : products) {
			if (!PfdiUtil.isProductValid(product, product.getEffectiveStartDate(), product.getEffectiveEndDate())) {
				continue;
			}

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

			for (Size size : sizes) {


				IntegerField quantityField = null;

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
							quantityField = new IntegerField();

							int quantity = 0;

							ItemStock stock = itemStock.get(size.getSizeName());
							if (stock != null) {
								quantity = itemStock.get(size.getSizeName()).getAvailableStock();
							}
							quantityField.setMax(quantity);
							
							quantityField.addKeyDownListener(e-> {
								if (e.getKey().equals(Key.ARROW_DOWN)) {
									IntegerField field = (IntegerField) e.getSource();
									String[] id = field.getId().get().split("-");
									
									int productIndexParsd = Integer.parseInt(id[0]);
									int sizeIndexParsed = Integer.parseInt(id[1]);
									
									
									int pIndex = productIndexParsd + 1;
									
									String key = pIndex + "-" + sizeIndexParsed;
									itemMap.get(key).focus();
									
									
									
								}
							});

							String itemId = product.getProductShortCode() + "-" + size.getSizeName();

							quantityField.setId(itemId);
							quantityField.addClassName("span-order-size-column");
							quantityField.setHelperText("Stock: " + quantity);
							quantityField.setPlaceholder("0");
							quantityField.setMin(0);
							quantityField.addValueChangeListener(e -> {

								BigDecimal oldAmount = productPrice.getTransferPrice() != null ?
										productPrice.getTransferPrice().multiply(BigDecimal.valueOf(e.getOldValue() != null ? e.getOldValue() : 0)) : BigDecimal.ZERO;
								BigDecimal newAmount = 
										productPrice.getSuggestedRetailPrice() != null ? productPrice.getTransferPrice()
										.multiply(BigDecimal.valueOf(e.getValue() != null ? e.getValue() : 0)) : BigDecimal.ZERO;
								
								
								BigDecimal oldSrpAmount = productPrice.getSuggestedRetailPrice() != null ? 
										productPrice.getSuggestedRetailPrice().multiply(BigDecimal.valueOf(e.getOldValue() != null ? e.getOldValue() : 0)) : BigDecimal.ZERO;
								BigDecimal newSrpAmount = productPrice.getSuggestedRetailPrice() != null ? productPrice.getSuggestedRetailPrice()
										.multiply(BigDecimal.valueOf(e.getValue() != null ? e.getValue() : 0)): BigDecimal.ZERO;
										

								currentTotalPrice = currentTotalPrice.subtract(oldAmount);
								currentTotalPrice = currentTotalPrice.add(newAmount);
								
								currentTotalSrp = currentTotalSrp.subtract(oldSrpAmount);
								currentTotalSrp = currentTotalSrp.add(newSrpAmount);

								totalAmount.setText("Total Amount : " + PfdiUtil.getFormatter().format(currentTotalPrice));

//								add(totalAmount);

							});
							contentWrapper.add(quantityField);
							
							Items item = new Items(quantityField, productPrice.getTransferPrice(), stock, productPrice.getSuggestedRetailPrice(), itemId );
							System.out.println("item Id " + itemId);
							itemMap.put(itemId, quantityField);
							itemsList.add(item);
						}
					}
				} else {
					quantityField = new IntegerField();
					quantityField.setMax(0);
					quantityField.addClassName("span-order-size-column");
					quantityField.setHelperText("Stock: N/A");
					quantityField.setValue(0);
					quantityField.setMin(0);
					quantityField.setEnabled(false);
					contentWrapper.add(quantityField);

				}
			}

			layout.add(contentWrapper);
			productIndex++;

		}

		//add(layout);
		
		return layout;

	}

	public Set<OrderItems> createOrderItems(AppUser user, LocalDateTime localDateTime, Order order) throws IncorrectOrderException{
		Set<OrderItems> orderItems  = Sets.newHashSet();
		
		for (Items items : itemsList) {
			
			if (Objects.nonNull(items.getNumberField()) && Objects.nonNull(items.getNumberField().getValue()) && items.getNumberField().getValue() > 0) {
				
				if (items.getNumberField().isInvalid()) {
					throw new IncorrectOrderException("");
				}

				int quantity = items.getNumberField().getValue();

				OrderItems orderItem = new OrderItems();

				orderItem.setQuantity(quantity);

				ItemStock stock = items.getItemStock();
				int availableStock = stock.getAvailableStock();
				int adjustment = availableStock - quantity;
				stock.setAvailableStock(adjustment);
				orderItem.setItemInventory(stock);
				orderItem.setProductPrice(items.getPrice());
				orderItem.setProductSrp(items.getSrp());
				
				orderItem.setUpdatedDate(localDateTime);
				orderItem.setCreatedBy(user);
				orderItem.setCreatedDate(localDateTime);
				orderItem.setUpdatedBy(user);
				orderItem.setOrder(order);
				
				orderItems.add(orderItem);
			}
		}
		
		return orderItems;
	}

	public BigDecimal getTotalAmount() {
		return currentTotalPrice;
	}
	
	public BigDecimal getTotalSrpAmount() {
		return currentTotalSrp;
	}

	private class Items {

		private IntegerField numberField;
		private BigDecimal price;
		private ItemStock itemStock;
		private BigDecimal srp;
		//private String itemId;

		public Items(IntegerField numberField, BigDecimal price, ItemStock itemStock, BigDecimal srp, String itemId) {
			super();
			this.numberField = numberField;
			this.price = price;
			this.itemStock = itemStock;
			this.srp = srp;
		//	this.itemId = itemId;
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

		public BigDecimal getSrp() {
			return srp;
		}
		
		
	}

	public void setOrderItems(Set<OrderItems> orderItems) {
		this.orderItems = orderItems;

		setValues();
	}

	public Set<OrderItems> updateOrderItems(AppUser user, Order order) {
		//setValues();
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
						orderItem.setOrder(order);
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
			return productShortCode + "-" + sizeName;
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
