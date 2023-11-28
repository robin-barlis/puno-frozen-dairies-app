package com.example.application.views.products;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import javax.annotation.security.PermitAll;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.cloudinary.Cloudinary;
import com.cloudinary.Singleton;
import com.cloudinary.utils.ObjectUtils;
import com.example.application.data.Categories;
import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.ProductPrice;
import com.example.application.data.entity.products.Size;
import com.example.application.data.entity.stock.ItemStock;
import com.example.application.data.service.products.CategoryService;
import com.example.application.data.service.products.ProductPriceService;
import com.example.application.data.service.products.ProductService;
import com.example.application.data.service.products.SizesService;
import com.example.application.data.service.stock.ItemStockService;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.products.components.SizePricingSubView;
import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.StreamResource;

@PageTitle("Products")
@Route(value = "addProduct/:productId?", layout = MainLayout.class)
@RouteAlias(value = "/addProduct", layout = MainLayout.class)
@PermitAll
public class AddNewProductView extends AbstractPfdiView implements HasComponents, HasStyle,HasUrlParameter<String>  {

	private static final long serialVersionUID = -6210105239749320428L;

	private TextField productName;
	private TextField shortCode;
	private DatePicker effectiveStartDate;
	private DatePicker effectiveEndDate;
	private Select<Category> category;
	private MultiSelectComboBox<Size> sizes;
	private Button cancelButton;
	//private Button saveAsDraftButton;
	private Button publishButton;
	private ProductService productService;
	private HashSet<SizePricingSubView> pricingSubViewSet = new HashSet<>();
	private ItemStockService itemStockService;
	private ProductPriceService priceService;
	private CheckboxGroup<String> checkboxGroup;
	Accordion sizesAccordion = new Accordion();
	
	Binder<Product> binder = new Binder<>();
	private Integer productId;
	List<Category> categories;
	Set<ProductPrice> productPrice= null;
	File file = null;

	private Product product;
	
	private final static int MAX_FILE_SIZE_BYTES = 2000 * 10 * 1024 * 1024; // 10MB
	private final Cloudinary cloudinary = Singleton.getCloudinary();
	private String imageUrl = "https://res.cloudinary.com/dw2qyhgar/image/upload/v1676078906/170043505_10158971498776278_8359436008848051948_n_jknb5u.jpg";
	private Image image;
	@Autowired
	public AddNewProductView(AuthenticatedUser authenticatedUser, ProductPriceService priceService, CategoryService categoryService, ProductService productService, SizesService sizesService, ItemStockService itemStockService) {
		super("add-new-product", "Add New Product");
		addClassNames("products-view");
		this.productService = productService;
		this.itemStockService = itemStockService;
		this.priceService = priceService;

		this.categories = categoryService.listAll(Sort.by("id"));
		VerticalLayout content = new VerticalLayout();
		createMainContent(content);
		add(content);

	}

	private void createMainContent(VerticalLayout content) {
		HorizontalLayout categoryNameImageWrapper = new HorizontalLayout();
		categoryNameImageWrapper.setWidth("100%");
		categoryNameImageWrapper.setHeight("200px");
		
		FlexLayout headerNameWrapper = new FlexLayout();
		headerNameWrapper.setFlexDirection(FlexDirection.ROW);
		headerNameWrapper.setJustifyContentMode(JustifyContentMode.START);
		headerNameWrapper.setAlignItems(Alignment.CENTER);
		H2 header = new H2("Product Details");
		header.addClassNames("mb-0", "mt-s", "text-xl");
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		VerticalLayout layout = new VerticalLayout();
		layout.setHeight("141px");
		layout.setWidth("250px");
		
        image = new Image();
        image.setHeight("150px");
        image.setWidth("180px");
        image.setSrc(imageUrl);
       // image.addClassName("product-view-image-attributes");
		

		MemoryBuffer memoryBuffer = new MemoryBuffer();

		Upload uploadImage = new Upload();
		uploadImage.setDropAllowed(false);
		uploadImage.setHeightFull();
		uploadImage.setWidthFull();
		uploadImage.setAcceptedFileTypes("image/*");
		uploadImage.setMaxFiles(1);
		uploadImage.setMaxFileSize(MAX_FILE_SIZE_BYTES);
		UploadProductImageI18N i18N = new UploadProductImageI18N();
	   
	    uploadImage.setI18n(i18N);

		uploadImage.setReceiver(memoryBuffer);
		uploadImage.addFailedListener(event -> {
			String errorMessage = event.getReason().getMessage();

			Notification notification = Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
		});
		uploadImage.addFileRejectedListener(event -> {
			String errorMessage = event.getErrorMessage();

			Notification notification = Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
		});
		uploadImage.addSucceededListener(event -> {
			
			InputStream fileData = memoryBuffer.getInputStream();
			String fileName = event.getFileName();	

			try {
				BufferedImage bufferedImage = ImageIO.read(fileData);
				file = new File(fileName);
				file.createNewFile();
				
				
				if (file.exists()) {
					ImageIO.write(bufferedImage, FilenameUtils.getExtension(fileName), file);

					StreamResource imageResource = new StreamResource("profilePicture",
							() -> memoryBuffer.getInputStream());

					image.setSrc(imageResource);
					//cloudinary.uploader().destroy(fileName, uploadResult);

				}
				
				

			} catch (IOException e1) {
				e1.printStackTrace();
				Notification notification = Notification.show("Upload failed. Please try again. If issue persist, please contact system administrator.", 5000, Notification.Position.BOTTOM_START);
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			} 

		});
		
		Button uploadButton = new Button();
		uploadButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
		uploadButton.setIcon(image);
		uploadButton.setSizeFull();
		uploadImage.setUploadButton(uploadButton);
		uploadButton.setTooltipText("Image must be 10 MB or below.");
		layout.add(uploadImage);
		
		

		VerticalLayout newAddSizeContainer = new VerticalLayout();
		VerticalLayout nameAndCategoryWrapper = new VerticalLayout();
		nameAndCategoryWrapper.addClassName("add-product-name-wrapper");
		
		productName = new TextField("Product Name");
		productName.setWidthFull();
		productName.setRequired(true);
		productName.setRequiredIndicatorVisible(true);
		productName.setVisible(true);
		
		shortCode = new TextField("Product Short Code");
		shortCode.setWidthFull();
		shortCode.setRequired(true);
		shortCode.setRequiredIndicatorVisible(true);
		shortCode.setVisible(true);
		
		effectiveStartDate = new DatePicker("Effective Start Date");
		effectiveStartDate.setWidthFull();
		effectiveStartDate.setRequired(true);
		effectiveStartDate.setRequiredIndicatorVisible(true);
		effectiveStartDate.setVisible(true);
		effectiveStartDate.setValue(LocalDate.now());


		checkboxGroup = new CheckboxGroup<>();
		checkboxGroup.setLabel("Status");
		checkboxGroup.setItems("Active");
		checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
		checkboxGroup.setEnabled(true);
		checkboxGroup.setValue(Sets.newHashSet("Active"));
		
		
		effectiveEndDate = new DatePicker("Effective End Date");
		effectiveEndDate.setWidthFull();
		effectiveEndDate.setRequired(true);
		effectiveEndDate.setRequiredIndicatorVisible(true);
		effectiveEndDate.setVisible(true);

		effectiveEndDate.setValue(LocalDate.now().plusYears(100));
		effectiveEndDate.addValueChangeListener(event -> {
		    LocalDate effectiveStartDateValue = effectiveStartDate.getValue();
		    String errorMessage = null;
		    LocalDate currentValue = event.getValue();
		    if (effectiveStartDateValue != null && currentValue != null) {
		        if (effectiveStartDateValue.compareTo(currentValue) < 0) {
		            errorMessage = "Effective End Date is earlier than Effective Start Date.";
		            effectiveEndDate.setInvalid(true);
		        } else {
		        	 effectiveEndDate.setInvalid(false);
		        }
		    }
		    effectiveEndDate.setErrorMessage(errorMessage);
		});
		
		
		HorizontalLayout productInfoContainer = new HorizontalLayout();
		productInfoContainer.setAlignItems(Alignment.AUTO);
		productInfoContainer.setSizeFull();
		productInfoContainer.add(productName, shortCode, effectiveStartDate, effectiveEndDate);
		

		
		category = new Select<>();
		category.setLabel("Category");
		category.setEmptySelectionAllowed(false);
		category.setItems(categories);
		category.setRequiredIndicatorVisible(true);
		category.setEmptySelectionAllowed(false);
		category.setPlaceholder("Select Category");
		category.setItemLabelGenerator(e -> e.getCategoryName());
		category.setWidthFull();
		category.addValueChangeListener(e -> {
			if (!e.getHasValue().isEmpty()) {
				if (Categories.Flavors.name().equals(e.getValue().getCategoryType())) {
					productName.setLabel("Flavor Name");
					shortCode.setLabel("Flavor Short Code");
				} else if (Categories.Cones.name().equals(e.getValue().getCategoryType())) {
					productName.setLabel("Cone Name");
					shortCode.setLabel("Cone Short Code");
				} else {
					productName.setLabel("Other Product Name");
					shortCode.setLabel("Other Product Short Code");
				}
				sizes.setItems(e.getValue().getSizeSet());
				productName.setVisible(true);
				shortCode.setVisible(true);
				pricingSubViewSet.clear();
				newAddSizeContainer.removeAll();
				
			}
		});
		
		sizes = new MultiSelectComboBox<>();
		sizes.setLabel("Sizes");
		sizes.setPlaceholder("Select Sizes");
		sizes.setItemLabelGenerator(e -> e.getSizeName());
		sizes.setWidthFull();
		newAddSizeContainer.add(sizesAccordion);
		sizes.addValueChangeListener(e -> {
			
			Set<Size> newValue = e.getValue();
			Set<Size> oldValue = e.getOldValue();
			
			
			Set<Size> added = Sets.difference(newValue, oldValue);
			Set<Size> deleted = Sets.difference(oldValue, newValue);
			
			for (Size size : added) {
				Map<String, ProductPrice> sizeProductPrice = null;
				
				if (productPrice != null) {
					sizeProductPrice = productPrice.stream()
							.filter(ps -> ps.getSize().getId()
							.equals(size.getId()))
							.collect(Collectors.toMap(keyObj -> {
								
								String key = keyObj.getCustomerTagId() + "-" + keyObj.getLocationTagId();
								return key;
							}, Function.identity()));
							
				}
				
				addNewSizeFormSection(newAddSizeContainer, size, sizeProductPrice);
			}
			
			for (Size removedSize : deleted) {
				
				Set<SizePricingSubView> viewToDelete = Sets.newHashSet();
				pricingSubViewSet.forEach(subView -> {
					if (subView.getSize().getSizeName().equalsIgnoreCase(removedSize.getSizeName())) {
						sizesAccordion.remove(subView);
						viewToDelete.add(subView);
					}
				});
				
				pricingSubViewSet.removeAll(viewToDelete);
			}
		}); //TODO add a prompt that will ask the user if they want to change the selection
		
		binder.forField(productName)
			.asRequired("Product Name must not be empty")
			.bind(Product::getProductName,Product::setProductName);
		binder.forField(category)
			.asRequired("Category must not be empty")
			.bind(Product::getCategory,Product::setCategory);
		binder.forField(sizes)
			.asRequired("Sizes must not be empty");
		binder.forField(shortCode)
			.asRequired("Product Short Code must not be empty")
			.bind(Product::getProductShortCode,Product::setProductShortCode);
		binder.forField(effectiveStartDate)
			.asRequired("Effective Start Date must not be empty")
			.bind(Product::getEffectiveStartDate,Product::setEffectiveStartDate);
		binder.forField(effectiveEndDate)
			.asRequired("Effective End Date must not be empty")
			.withValidator(
					effectiveEndDateValue -> effectiveStartDate.getValue().compareTo(effectiveEndDateValue) < 0, 
					"Effective End Date must not be earlier than Effective Start Date")
			.bind(Product::getEffectiveEndDate,Product::setEffectiveEndDate);
		binder.forField(checkboxGroup).bind(e -> {
				boolean isActiveStatus = e.getActiveStatus() != null && e.getActiveStatus();
				if  (isActiveStatus) {
					return Sets.newHashSet("Active");
				}
				return Sets.newHashSet();
				
			}, (p, sets) -> {
				if (sets != null && sets.size() == 1) {
					p.setActiveStatus(sets.contains("Active"));
				} else {
					p.setActiveStatus(false);
				}
			});
		
		HorizontalLayout productSpecsContainer = new HorizontalLayout();
		productSpecsContainer.setAlignItems(Alignment.END);
		productSpecsContainer.setSizeFull();
		productSpecsContainer.add(category, sizes, checkboxGroup);

		nameAndCategoryWrapper.add(productSpecsContainer, productInfoContainer);

		VerticalLayout sizeContainer = new VerticalLayout();

		VerticalLayout iceCreamDescriptionContainer = new VerticalLayout();
		iceCreamDescriptionContainer.setAlignItems(Alignment.START);
		iceCreamDescriptionContainer.add(nameAndCategoryWrapper);

		categoryNameImageWrapper.add(layout, nameAndCategoryWrapper);

		HorizontalLayout actionsButtonLayout = new HorizontalLayout();
		actionsButtonLayout.setAlignItems(Alignment.END);
		actionsButtonLayout.setJustifyContentMode(JustifyContentMode.END);
		actionsButtonLayout.addClassNames("padding-top-large", "full-width");
		cancelButton = new Button("Cancel");
		
		cancelButton.addClickListener(e -> {
//			newAddSizeContainer.removeAll();
//			category.setValue(null);
//			productName.setValue(null);
			UI.getCurrent().navigate(ProductsView.class);
			
		});
		publishButton = new Button("Publish");
		publishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		publishButton.addClickListener(e -> {

			if (productName.isInvalid() && shortCode.isInvalid() && category.isInvalid() && effectiveStartDate.isInvalid() && effectiveEndDate.isInvalid()) {
				
			} else {
				
			}
			
			try {

	            product = createProduct();
	            binder.writeBean(product);

				try {
					if (file != null) {

						@SuppressWarnings("rawtypes")
						Map response = cloudinary.uploader().upload(file, null);
						String url = (String) response.get("url");
						product.setProductPictureUrl(url);
						
						System.out.println(response);
					}
				} catch (IOException e1) {

					Notification.show("Could not save image on the repository: " + product.getProductName());
				}
				
				if (product.getProductPrices().isEmpty()) {
					product.setActiveStatus(false);
				}
				product = productService.update(product);
				
				
				if (productId == null) {

					itemStockService.updateAll(createInitialItemStockData(product));
				}

				Notification.show("Successfully updated the product: " + product.getProductName());
				UI.getCurrent().navigate(ProductsView.class); 
	        } catch (ValidationException validationException) {
				Notification.show("Could not create new product. Please check if fields are valid.");
				//UI.getCurrent().navigate(ProductsView.class); 
	        }
		});
		
		
		actionsButtonLayout.add(cancelButton, publishButton);

		newAddSizeContainer.addClassNames("no-padding");
		VerticalLayout addSizeButtonLayout = new VerticalLayout();
		addSizeButtonLayout.addClassNames("no-padding", "padding-top-large");
		//addSizeButtonLayout.add(addSizeButton);
		addSizeButtonLayout.add(actionsButtonLayout);
		
		sizesAccordion.setWidthFull();

		content.add(categoryNameImageWrapper, sizeContainer, sizesAccordion, addSizeButtonLayout);

	}

	private List<ItemStock> createInitialItemStockData(Product product) {
		
		
		ArrayList<ItemStock> itemStocks = new ArrayList<ItemStock>();
		
		for (Size size : sizes.getValue()) {
			ItemStock itemStock = new ItemStock();
			itemStock.setProduct(product);
			itemStock.setSize(size);
			itemStock.setUpdatedBy("test");
			itemStock.setAvailableStock(0);
			itemStock.setReservedStock(0);
			itemStocks.add(itemStock);
		}
		
		// TODO Auto-generated method stub
		return itemStocks;
	}

	private Product createProduct() {
		if (product == null) {
			product = new Product();
			product.setSortingIndex(0);

			if (file != null) {
				try {
					Map<?, ?>  uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());

					String url = uploadResult.get("url").toString();
					
					product.setProductPictureUrl(url);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Notification notification = Notification.show("Upload faileds. Please try again. If issue persist, please contact system administrator.", 5000, Notification.Position.MIDDLE);
					notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				}
				
			}
		} else {
			// delete all product prices first before adding new product
			product.getProductPrices().forEach(e -> {
				if (e.getId() != null) {
					// price has already been inserted to the database
					priceService.delete(e.getId());
				}
			});
		}
		

		product.setProductDescription("product test"); //TODO add field description
		HashSet<ProductPrice> prices = new HashSet<>();
		for (SizePricingSubView pricingView : pricingSubViewSet ) {
			
			prices.addAll(pricingView.extractFieldValues(product));
			
		}
		product.setProductPrices(prices);	
		return product;
		
	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContent) {

	}

	private void addNewSizeFormSection(VerticalLayout addSizeButtonLayout, Size size, Map<String, ProductPrice> sizeProductPrice) {
		SizePricingSubView pricingSubView = new SizePricingSubView(size, category.getValue(), sizeProductPrice);
		pricingSubViewSet.add(pricingSubView);
		//addSizeButtonLayout.add(pricingSubView);
		sizesAccordion.add(size.getSizeName(), pricingSubView);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

	}
	

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		RouteParameters params = event.getRouteParameters();
		String productIdString = params.get("productId").orElse(null);
		
		if (productIdString != null) {
			productId = Integer.parseInt(productIdString);
			
			product = productService.get(productId).orElse(null);

			if (product != null) {
				if  (product.getActiveStatus()) {
					checkboxGroup.setValue(Sets.newHashSet("Active"));
				} else {
					checkboxGroup.setValue(Sets.newHashSet());
				}
				productPrice = product.getProductPrices();
				if (product != null) {
					productName.setValue(product.getProductName());
					
					if (product.getProductPictureUrl() != null) {
						image.setSrc(product.getProductPictureUrl());
					} else {
						image.setSrc(imageUrl);
					}
					shortCode.setValue(product.getProductShortCode());
					effectiveStartDate.setValue(product.getEffectiveStartDate());
					effectiveEndDate.setValue(product.getEffectiveEndDate());
					Optional<Category> optionalCategory = categories.stream().filter(category -> category.getId().equals(product.getCategory().getId())).findFirst();
					if (optionalCategory.isPresent()) {
						category.setValue(optionalCategory.get());
					}
					
					
					Set<Integer> sizesMap = product.getProductPrices().stream().map(e-> e.getSize().getId()).collect(Collectors.toSet());
					Set<Size> productSizes = category.getValue().getSizeSet().stream().filter(catSizes -> sizesMap.contains(catSizes.getId())).collect(Collectors.toSet());
					sizes.setValue(productSizes);
					
				}
			}
			
		}
		
	}
}