package com.example.application.views.products.components;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.Size;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ProductsViewCard extends ListItem {

    private static final long serialVersionUID = 2711778627059486451L;
 


	public ProductsViewCard(Product product, Category category, String url) {
        addClassNames("bg-contrast-5", "flex", "flex-col", "items-start", "rounded-l");

        Div div = new Div();
        div.addClassNames("background-div", "flex items-center", "justify-center", "mb-m", "overflow-product-view-card",
                "rounded-top w-full");
        div.setHeight("242px");

        Image image = new Image();
        image.setWidth("100%");
        image.setSrc(url);
        image.setAlt(product.getProductName());
        image.addClassName("product-view-image-attributes");

        div.add(image);
        

        Span iceCreamName = new Span();
        iceCreamName.addClassNames("text-l", "font-semibold");
        iceCreamName.setText(product.getProductName());
        
        
        Set<String> sizes = category.getSizeSet().stream().map(e->e.getSizeName()).collect(Collectors.toSet());
        Paragraph sizesContainer = new Paragraph(String.join(" | ", sizes));
        sizesContainer.addClassName("my-m");

        Span categorySpan = new Span();
        categorySpan.getElement().setAttribute("theme", "badge");
        categorySpan.setText(category.getCategoryName());
        
        VerticalLayout iceCreamDescriptionContainer = new VerticalLayout();
        iceCreamDescriptionContainer.setAlignItems(Alignment.START);
        iceCreamDescriptionContainer.add(iceCreamName, sizesContainer, categorySpan );

        add(div, iceCreamDescriptionContainer);

    }
}
