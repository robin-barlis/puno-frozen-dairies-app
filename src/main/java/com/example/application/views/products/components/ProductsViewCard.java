package com.example.application.views.products.components;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Product;
import com.example.application.views.products.AddNewProductView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;

public class ProductsViewCard extends ListItem {

    private static final long serialVersionUID = 2711778627059486451L;
 


	public ProductsViewCard(Product product, Category category, String url) {
        addClassNames("bg-contrast-5", "flex", "flex-col", "items-start", "rounded-l");

        Div imageDiv = new Div();
        imageDiv.addClassNames("background-div", "flex items-center", "justify-center", "mb-m", "overflow-product-view-card",
                "rounded-top w-full");
        imageDiv.setHeight("242px");

        Image image = new Image();
        image.setWidth("100%");
        image.setSrc(url);
        image.setAlt(product.getProductName());
        //image.addClassName("product-view-image-attributes");

        imageDiv.add(image);
        

        Span iceCreamName = new Span();
        iceCreamName.addClassNames("text-l", "font-semibold");
        iceCreamName.setText(product.getProductName());
        VerticalLayout iceCreamDescriptionContainer = new VerticalLayout();

        iceCreamDescriptionContainer.add(iceCreamName);
        
        Span categorySpan = new Span();
        categorySpan.getElement().setAttribute("theme", "badge");
        categorySpan.setText(product.getCategory().getCategoryName());
        
        
        if (category != null) {

            Set<String> sizes = category.getSizeSet().stream().map(e->e.getSizeName()).collect(Collectors.toSet());
            Paragraph sizesContainer = new Paragraph(String.join(" | ", sizes));
            sizesContainer.addClassName("my-m");



            iceCreamDescriptionContainer.setAlignItems(Alignment.START);
            iceCreamDescriptionContainer.add(sizesContainer);
        }
        
        iceCreamDescriptionContainer.add(categorySpan);
        
       RouterLink productLink = new RouterLink();
        

    	RouteParam param = new RouteParam("productId", product.getId().toString());
    	RouteParameters routeParams = new RouteParameters(param);
        
        productLink.setRoute(AddNewProductView.class, routeParams);
        productLink.setQueryParameters(null);
        productLink.add(imageDiv);
        productLink.setClassName("product-card-image-link");
        
        
        

        add(productLink, iceCreamDescriptionContainer);

    }
}
