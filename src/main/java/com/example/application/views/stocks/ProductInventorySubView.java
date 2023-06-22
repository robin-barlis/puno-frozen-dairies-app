package com.example.application.views.stocks;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Lists;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.ProductPrice;
import com.example.application.data.entity.products.Size;
import com.example.application.data.entity.stock.ItemStock;
import com.example.application.utils.PfdiUtil;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

public class ProductInventorySubView extends VerticalLayout {

	private static final long serialVersionUID = -6013987573691278385L;
	private Product product;
	private Map<String, ItemStock> itemStockMap = null;
	private AppUser user;
	private List<ItemStockFieldMapping> itemStockMappingList = Lists.newArrayList();

	public ProductInventorySubView(Product product, AppUser user) {
		this.product = product;
		this.itemStockMap = PfdiUtil.createSizeMap(product);
		this.user = user;
		createContent();

	}

	private void createContent() {
		this.addClassNames("sub-pricing-wrapper");

		Set<Size> sizes = product.getProductPrices().stream().map(ProductPrice::getSize).collect(Collectors.toSet());
		HorizontalLayout productNameContainer = new HorizontalLayout();
		
		H5 productName = new H5(product.getProductName());
		productNameContainer.add(productName);
		
		add(productNameContainer);
		
		if (sizes.size() == 0) {
			H5 noSize = new H5("No Available Sizes for this product");
			add(noSize);
		}
		for (Size productPrizePerSize : sizes) {
			add(createStockLayout(productPrizePerSize));
		}

	}

	private HorizontalLayout createStockLayout(Size size) {
		System.out.println("creating stock layout");
		HorizontalLayout tagPriceWrapper = new HorizontalLayout();
		tagPriceWrapper.setWidthFull();

		VerticalLayout customerTagWrapper = new VerticalLayout();
		customerTagWrapper.setWidth("70%");

		TextField customerTagField = new TextField();
		customerTagField.setValue(size.getSizeName());
		customerTagField.setReadOnly(true);
		customerTagField.setLabel("Size");
		customerTagField.setWidthFull();

		VerticalLayout locationTagWrapper = new VerticalLayout();

		HorizontalLayout priceTextFieldWrapper = new HorizontalLayout();
		priceTextFieldWrapper.setWidthFull();
		IntegerField availabelUnits = new IntegerField();
		availabelUnits.setLabel("Available Units");
		availabelUnits.setWidthFull();
		availabelUnits.setReadOnly(true);
		
		ItemStock itemStock = itemStockMap.get(size.getSizeName());
		
		if (itemStock == null) {
			itemStock = createNewItemStock(size);
		}
		
		Integer currentUnit = itemStock.getAvailableStock();
		availabelUnits.setValue(currentUnit);
		
		IntegerField unitAdjustment = new IntegerField();
		unitAdjustment.setLabel("Adjustment");
		unitAdjustment.setWidthFull();
		unitAdjustment.setRequiredIndicatorVisible(true);
		unitAdjustment.setPlaceholder("0");

		priceTextFieldWrapper.add(availabelUnits);
		priceTextFieldWrapper.add(unitAdjustment);
		locationTagWrapper.add(priceTextFieldWrapper);

		customerTagWrapper.add(customerTagField);
		tagPriceWrapper.add(customerTagWrapper, locationTagWrapper);
		
		ItemStockFieldMapping fieldMapping = new ItemStockFieldMapping(itemStock, unitAdjustment);
		itemStockMappingList.add(fieldMapping);
		return tagPriceWrapper;
	}

	private ItemStock createNewItemStock(Size size) {
		ItemStock stock = new ItemStock();
		stock.setProduct(product);
		stock.setSize(size);
		stock.setAvailableStock(0);
		stock.setReservedStock(0);
		stock.setUpdatedBy(user.getUsername());
		itemStockMap.put(size.getSizeName(), stock);
		return stock;
	}

	public HashSet<ItemStock> extractFieldValues() {

		HashSet<ItemStock> itemStockSet = new HashSet<>();
		
		for (ItemStockFieldMapping itemStockField : itemStockMappingList) {
			Integer value = itemStockField.getAdjusmentField().getValue() != null ?itemStockField.getAdjusmentField().getValue() :0 ;
			ItemStock stock = itemStockField.getItemStock();
			
			Integer adjustment = stock.getAvailableStock() != null ? stock.getAvailableStock() : 0;
			
			stock.setAvailableStock(value + adjustment);
			itemStockSet.add(stock);
			
		}
		
		return itemStockSet;

	}
	
	private class ItemStockFieldMapping {

		private ItemStock itemStock;
		private IntegerField adjusmentField;
		
		public ItemStockFieldMapping(ItemStock itemStock, IntegerField adjusmentField) {
			super();
			this.itemStock = itemStock;
			this.adjusmentField = adjusmentField;
		}

		public ItemStock getItemStock() {
			return itemStock;
		}

		public IntegerField getAdjusmentField() {
			return adjusmentField;
		}
	
	}
	
	public Product getProduct() {
		return product;
	}


}
