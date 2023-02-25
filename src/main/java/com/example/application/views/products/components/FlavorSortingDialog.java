package com.example.application.views.products.components;

import java.util.List;
import java.util.stream.Collectors;

import org.vaadin.jchristophe.SortableConfig;
import org.vaadin.jchristophe.SortableGroupStore;
import org.vaadin.jchristophe.SortableLayout;

import com.example.application.data.Categories;
import com.example.application.data.entity.products.Product;
import com.example.application.data.service.products.ProductService;
import com.example.application.views.products.ProductsView;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class FlavorSortingDialog extends Dialog {
	
	private static final long serialVersionUID = 1L;
	
	private List<Product> products;
	private ProductService productService;
	
    private SortableLayout todoSortableLayout;
	
	
	public FlavorSortingDialog(List<Product> products, ProductService productService) {
		this.products = products;
		this.productService = productService;
		
		createDialog();
	}

	private void createDialog() {
		this.setWidth("800px");
		
		Label addProfileLabel = new Label("Drag cards to sort flavors");
		
		addProfileLabel.getStyle().set("padding-bottom", "20px");

		Hr divider1 = new Hr();
		
		addClassName("trello");

        SortableConfig sortableConfig = new SortableConfig();
        sortableConfig.setGroupName("trello");
        sortableConfig.allowDragIn(true);
        sortableConfig.allowDragOut(true);
        sortableConfig.setAnimation(150);
        sortableConfig.setChosenClass("trello-sortable-chosen");
        sortableConfig.setDragClass("trello-sortable-drag");
        sortableConfig.setGhostClass("trello-sortable-ghost");
        SortableGroupStore group = new SortableGroupStore();
        TrelloColumn todoColumn = new TrelloColumn("");
        addTasks(todoColumn);

        todoSortableLayout = new SortableLayout(todoColumn, sortableConfig, group);
      
        Button saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(e -> {
			
			List<Component> sortedFlavors = todoSortableLayout.getChildren().collect(Collectors.toList());	
			int index = 1;
			
			List<Product> updatedProduct = Lists.newArrayList();
			
			if (sortedFlavors != null && sortedFlavors.size() == 1) {
				List<Component> tasks = sortedFlavors.get(0).getChildren().collect(Collectors.toList());
				
				for (Component sort : tasks) {
					if (sort instanceof TrelloTask) {
						TrelloTask task = (TrelloTask) sort;
						Product product = task.getProduct();
						product.setSortingIndex(index);
						updatedProduct.add(product);
						index++;
						
					}
				}
			}
			
			productService.updateAll(updatedProduct);

			Notification.show("Product Sorting successfully updated").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			this.close();	
			UI.getCurrent().navigate(ProductsView.class);
		
		});

		Button cancelButton = new Button("Cancel");
		cancelButton.addClickListener(e -> {
			this.close();
		});
		
		Hr divider2 = new Hr();
		
		FormLayout buttonContainer = new FormLayout();
		buttonContainer.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));
		buttonContainer.setWidthFull();
		buttonContainer.add(cancelButton, saveButton);

        add(addProfileLabel,divider1, todoSortableLayout, divider2, buttonContainer);
    }

    private void addTasks(TrelloColumn todoColumn) {
    	
    	List<Product> flavors = products.stream().filter(e -> Categories.Flavors.name().equals(e.getCategory().getCategoryType())).collect(Collectors.toList());
        for (Product product : flavors) {
            todoColumn.add(new TrelloTask(product));
        }
    }

	@Override
	public void open() {
		
		super.open();
	}
	
	private class TrelloColumn extends Div {

	    private static final long serialVersionUID = 1L;


	    public TrelloColumn(String title) {
	        Div titleBox = new Div();
	        titleBox.setText(title);
	        add(titleBox);
	    }
	}
	
	private class TrelloTask extends Div {

		    private static final long serialVersionUID = 1L;
			private Product product;

		    public TrelloTask(Product product) {
		    	this.product = product;
		        Div titleBlock = new Div();
		        titleBlock.setText(product.getProductName().toUpperCase());
		        titleBlock.addClassName("trello__column__task__title");
		        Div descriptionBlock = new Div();
		        descriptionBlock.setText(product.getProductShortCode());
		        descriptionBlock.addClassName("trello__column__task__description");
		        
		        Div categoryBlock = new Div();
		        categoryBlock.setText(product.getCategory().getCategoryName());
		        categoryBlock.addClassName("trello__column__task__description");

		        add(titleBlock, descriptionBlock, categoryBlock);
		        addClassName("trello__column__task");
		    }

		    public Product getProduct() {
		        return product;
		    }
		}

}
