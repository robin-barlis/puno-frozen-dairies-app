package com.example.application.views.order;

import java.util.List;


import org.springframework.data.domain.Sort;

import com.example.application.data.DiscountType;
import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.Size;
import com.example.application.data.service.products.ProductService;
import com.example.application.views.order.offerings.AbstractOffering;
import com.example.application.views.order.offerings.BuyGetOffering;
import com.example.application.views.order.offerings.FixedAmountPercentageDiscount;
import com.example.application.views.order.offerings.FreeItemsOffering;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;

public class DiscountsDialogPage extends Dialog{

	private static final long serialVersionUID = 1L;

	private Button saveButton;
	private Button cancelButton;
	private Select<DiscountType> discountTypeSelect;
	
	private Select<Product> getProducts;

	private MultiSelectComboBox<Size> getSizes;
	
	private IntegerField buyQuanity;
	private IntegerField getQuantity;
	
	private NumberField fixedAmount;
	private VerticalLayout fixedAmountContent = new VerticalLayout();
	private VerticalLayout percentageContent = new VerticalLayout();
	private VerticalLayout buygetContent = new VerticalLayout();
	private VerticalLayout freeItemsContent = new VerticalLayout();
	
	
	private Select<Product> freeItemsProduct;

	private MultiSelectComboBox<Size> freeItemsSizes;
	
	private IntegerField freeItemQuantity;

	private NumberField percentageField;
	
	private ProductService productService;

	
	private boolean isSaveEnabled;
	
	private AbstractOffering offering;
	
	
	
	@Override
	public void open() {
		offering = null;
		super.open();
	}
 	
	
	public DiscountsDialogPage(ProductService productService) {
		this.setHeaderTitle("Add Discount");
		this.productService = productService;
		
		createDialogContent();
		createFixedAmountContent();
		createPercentageContent();
		createBuyGetContent();
		createFreeItemsContent();
		
		getFooter().add(cancelButton);
		getFooter().add(saveButton);
		saveButton.setEnabled(isSaveEnabled);
		saveButton.addClickListener(e -> {
			DiscountType discountType= discountTypeSelect.getValue();
			
			if (DiscountType.FixedAmount == discountType) {
				FixedAmountPercentageDiscount fixedAmountOffering = new FixedAmountPercentageDiscount(discountType, fixedAmount.getValue());
				
				offering = fixedAmountOffering;

			}
			
			if (DiscountType.Percentage == discountType) {
				FixedAmountPercentageDiscount percentageOffering = new FixedAmountPercentageDiscount(discountType, 
						percentageField.getValue());
				
				offering = percentageOffering;

			}
			
//			if (DiscountType.BuyGetDiscount == discountType) {
//				BuyGetOffering fixedAmountOffering = 
//						new BuyGetOffering(discountType, getProducts.getValue(), getSizes.getValue(), buyQuanity.getValue(), getQuantity.getValue());
//				
//				offering = fixedAmountOffering;
//
//			}
//			
//			if (DiscountType.FreeItems == discountType) {
//				FreeItemsOffering fixedAmountOffering = 
//						new FreeItemsOffering(discountType, freeItemsProduct.getValue(), freeItemsSizes.getValue(), freeItemQuantity.getValue());
//				
//				offering = fixedAmountOffering;
//
//			}
			
			this.close();
			
			
		});
		
		cancelButton.addClickListener(e-> {
			offering = null;
			this.close();
		});
		this.setWidth("40%");
		this.setMaxHeight("70%");
	}


	private void createFreeItemsContent() {
		HorizontalLayout freeItemsContainer = new HorizontalLayout();
		freeItemsContainer.setWidthFull();
		freeItemsProduct = new Select<>();
		freeItemsProduct.setLabel("Select Product");
		freeItemsProduct.setWidth("40%");
		
		List<Product> products = productService.listAll(Sort.by("productName"));
		freeItemsProduct.setItems(products);
		freeItemsProduct.setItemLabelGenerator(Product::getProductName);
		freeItemsProduct.addValueChangeListener( e-> {
			if (e.getValue() != null) {
				freeItemsSizes.setItems(e.getValue().getCategory().getSizeSet());
			}
			freeItemsSizes.setEnabled(e.getValue() != null);
		});
		
		freeItemsSizes = new MultiSelectComboBox<>();
		freeItemsSizes.setLabel("Select Sizes");
		freeItemsSizes.setWidth("40%");
		freeItemsSizes.setItemLabelGenerator(Size::getSizeName);
		freeItemsSizes.setEnabled(false);
		freeItemsSizes.addValueChangeListener(e-> {
			freeItemQuantity.setEnabled(e.getOldValue() != null);
		});
		
		freeItemQuantity = new IntegerField();
		freeItemQuantity.setLabel("Quantity");
		freeItemQuantity.setWidth("20%");
		freeItemQuantity.setSuffixComponent(new Span("PCS"));
		freeItemQuantity.setEnabled(false);
		freeItemQuantity.addValueChangeListener(e-> {
			saveButton.setEnabled(e.getOldValue() != null);
		});
		
		freeItemsContainer.add(freeItemsProduct, freeItemsSizes, freeItemQuantity);
			
		freeItemsContent.setVisible(false);
		freeItemsContent.add(freeItemsContainer);
		add(freeItemsContent);
		
	}


	private void createFixedAmountContent() {
		
		fixedAmountContent.setWidthFull();
		
		fixedAmount = new NumberField();
		fixedAmount.setLabel("Fixed Amount Value");
		fixedAmount.setWidthFull();
		fixedAmount.setSuffixComponent(new Span("PHP"));
		fixedAmountContent.add(fixedAmount);
		fixedAmountContent.setVisible(false);
		fixedAmount.addValueChangeListener(e -> {
			saveButton.setEnabled(e.getValue() != null);
		});
		
		add(fixedAmountContent);
	}
	
	private void createPercentageContent() {
		
		percentageContent.setWidthFull();
		
		percentageField = new NumberField();
		percentageField.setLabel("Fixed Amount Value");
		percentageField.setWidthFull();
		percentageField.setSuffixComponent(new Span("%"));
		percentageContent.add(percentageField);
		percentageContent.setVisible(false);
		percentageField.addValueChangeListener( e-> {
			saveButton.setEnabled(e.getValue() != null);
		});
		
		add(percentageContent);
	}


	private void createBuyGetContent() {
		
		
		HorizontalLayout buyGetContainer = new HorizontalLayout();
		buyGetContainer.setWidthFull();
		getProducts = new Select<>();
		getProducts.setLabel("Select Product");
		getProducts.setWidth("50%");
		
		List<Product> products = productService.listAll(Sort.by("productName"));
		getProducts.setItems(products);
		getProducts.setItemLabelGenerator(Product::getProductName);
		getProducts.addValueChangeListener( e-> {
			if (e.getValue() != null) {
				getSizes.setItems(e.getValue().getCategory().getSizeSet());
			}
			getSizes.setEnabled(e.getValue() != null);
		});
		
		getSizes = new MultiSelectComboBox<>();
		getSizes.setLabel("Select Sizes");
		getSizes.setWidth("50%");
		getSizes.setItemLabelGenerator(Size::getSizeName);
		getSizes.setEnabled(false);
		getSizes.addValueChangeListener( e-> {
			buyQuanity.setEnabled(e.getValue() != null);
		});
		
		buyGetContainer.add(getProducts, getSizes);
		

		HorizontalLayout quantityContainer = new HorizontalLayout();
		quantityContainer.setWidthFull();
		buyQuanity = new IntegerField();
		buyQuanity.setLabel("Buy");
		buyQuanity.setWidth("50%");
		buyQuanity.setSuffixComponent(new Span("PCS"));
		buyQuanity.setEnabled(false);
		buyQuanity.addValueChangeListener( e-> {
			getQuantity.setEnabled(e.getValue() != null);
		});
		
		
		getQuantity = new IntegerField();
		getQuantity.setLabel("Get");
		getQuantity.setWidth("50%");
		getQuantity.setSuffixComponent(new Span("PCS"));
		getQuantity.setEnabled(false);
		getQuantity.addValueChangeListener( e-> {
			saveButton.setEnabled(e.getValue() != null);
		});
		
		
		quantityContainer.add(buyQuanity, getQuantity);
		
		
		buygetContent.setVisible(false);
		buygetContent.add(buyGetContainer);
		buygetContent.add(quantityContainer);
		add(buygetContent);
		
		
	}


	private void createDialogContent() {
		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		
		cancelButton =  new Button("Cancel");
		cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		
		
		VerticalLayout discountTypeSelectContainer = new VerticalLayout();
		fixedAmountContent.setWidthFull();
		discountTypeSelect = new Select<>();
		discountTypeSelect.setLabel("Select Discount Type");
		discountTypeSelect.setItems(DiscountType.values());
		discountTypeSelect.setItemLabelGenerator(DiscountType::getDiscountLabel);
		discountTypeSelect.setWidthFull();
		discountTypeSelect.addValueChangeListener( e-> {
			DiscountType value = e.getValue();

			fixedAmountContent.setVisible(DiscountType.FixedAmount == value);
			percentageContent.setVisible(DiscountType.Percentage == value);
			buygetContent.setVisible(DiscountType.BuyGetDiscount == value);
			freeItemsContent.setVisible(DiscountType.FreeItems == value);

		});
		discountTypeSelectContainer.add(discountTypeSelect);
		
		
		
		
		add(new Hr());
		add(discountTypeSelectContainer);
		
		
	}
	
	public AbstractOffering getGeneratedOffering() {
		return offering;
	}
	
	

}
