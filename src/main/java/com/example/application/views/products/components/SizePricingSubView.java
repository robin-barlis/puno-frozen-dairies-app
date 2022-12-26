package com.example.application.views.products.components;

import java.math.BigDecimal;
import java.util.HashSet;

import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.CustomerTag;
import com.example.application.data.entity.products.LocationTag;
import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.ProductPrice;
import com.example.application.data.entity.products.Size;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;

public class SizePricingSubView extends VerticalLayout {

	private static final long serialVersionUID = -6013987573691278385L;
	private Category category;
	private TextField sizeField;
	private Size size;	
	private ProductPrice productPrice = null;
	

	private HashSet<LocationTagPriceFieldMapping> fieldMapping = new HashSet<>();

	public SizePricingSubView(Size size, Category category, ProductPrice productPrice) {
		this.category = category;
		this.size = size;
		this.productPrice = productPrice;
		createContent();

	}

	private void createContent() {
		this.addClassNames("sub-pricing-wrapper");
		sizeField = new TextField();
		sizeField.setValue(size.getSizeName());
		sizeField.setReadOnly(true);
		sizeField.setWidthFull();
		sizeField.setLabel("Size");

		add(sizeField);
		
		for (CustomerTag customerTag : size.getCustomerTagSet()) {
			add(createSizeFormLayouts(customerTag));
		}

	}

	private HorizontalLayout createSizeFormLayouts(CustomerTag customerTag) {
		HorizontalLayout tagPriceWrapper = new HorizontalLayout();
		tagPriceWrapper.setWidthFull();

		VerticalLayout customerTagWrapper = new VerticalLayout();
		customerTagWrapper.setWidth("70%");
		
		TextField customerTagField = new TextField();
		customerTagField.setValue(customerTag.getCustomerTagName());
		customerTagField.setReadOnly(true);
		customerTagField.setLabel("Customer Tag");
		customerTagField.setWidthFull();
		
		
		VerticalLayout locationTagWrapper = new VerticalLayout();
			
		for (LocationTag locationTag : customerTag.getLocationTagSet()) {
			HorizontalLayout priceTextFieldWrapper = new HorizontalLayout();
			priceTextFieldWrapper.setWidthFull();
			BigDecimalField tpField = new BigDecimalField();
			tpField.setLabel(locationTag.getLocationTagName() + " - Transfer Price");
			tpField.setWidthFull();
			tpField.setRequiredIndicatorVisible(true);
			tpField.setPlaceholder("0.00");
			
			if (productPrice != null) {
				tpField.setValue(productPrice.getTransferPrice());
			}
			priceTextFieldWrapper.add(tpField);
			locationTagWrapper.add(priceTextFieldWrapper);	
			
			LocationTagPriceFieldMapping currentFieldMapping = new LocationTagPriceFieldMapping(locationTag.getId(), customerTag.getId(), size, tpField);
			fieldMapping.add(currentFieldMapping);
		}	

		customerTagWrapper.add(customerTagField);
		tagPriceWrapper.add(customerTagWrapper,locationTagWrapper);
		return tagPriceWrapper;
	}
	
	public HashSet<ProductPrice> extractFieldValues(Product product) {
		
		HashSet<ProductPrice> productPriceSet = new HashSet<>();
		for (LocationTagPriceFieldMapping locationTagPriceFieldMapping : fieldMapping) {

			ProductPrice productPrice = new ProductPrice();
			productPrice.setProduct(product);
			productPrice.setSize(locationTagPriceFieldMapping.getSizeId());
			productPrice.setCategoryId(category.getId());
			productPrice.setLocationTagId(locationTagPriceFieldMapping.getLocationTagId());
			productPrice.setCustomerTagId(locationTagPriceFieldMapping.getCustomerTagId());

			BigDecimal tp = locationTagPriceFieldMapping.getTransferPrice().getValue();
			productPrice.setTransferPrice(tp != null ? tp : new BigDecimal(0.0));
			productPriceSet.add(productPrice);
		}
		
		return productPriceSet;
		
	}
	
	private class LocationTagPriceFieldMapping {
		//private Integer categoryId;
		private Integer locationTagId;
		private Integer customerTagId;
		private Size size;
		private BigDecimalField transferPrice;
		
		private Integer getLocationTagId() {
			return locationTagId;
		}

		private Integer getCustomerTagId() {
			return customerTagId;
		}

		private Size getSizeId() {
			return size;
		}

		private BigDecimalField getTransferPrice() {
			return transferPrice;
		}

		public LocationTagPriceFieldMapping(Integer locationTagId, Integer customerTagId, Size size,
				BigDecimalField transferPrice) {
			super();
			this.locationTagId = locationTagId;
			this.customerTagId = customerTagId;
			this.size = size;
			this.transferPrice = transferPrice;

		}
		
	}
	
	public Size getSize() {
		return size;
	}

}
