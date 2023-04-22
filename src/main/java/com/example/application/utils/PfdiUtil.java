package com.example.application.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.example.application.data.Categories;
import com.example.application.data.CustomerTags;
import com.example.application.data.OrderStatus;
import com.example.application.data.Role;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.orders.OrderItems;
import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.CustomerTag;
import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.ProductPrice;
import com.example.application.data.entity.products.Size;
import com.example.application.data.entity.stock.ItemStock;
import com.google.gwt.thirdparty.guava.common.collect.Ordering;
import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.vaadin.flow.component.Component;

public class PfdiUtil {
	
	

	private final static Set<String> COMPANY_OWNED_TAGS = Sets.newHashSet("Relative Owned", "Company Owned", "Main Store");

	
	//T/C, 3.4L, 1.9L, 1.0L, PINT, CUP, I.C. CONE, 1.5L, 800ML, 475ML, 200ML
	private final static List<String> SIZE_NAMES = Arrays.asList("T/C", "3.4L", "1.9L", "1.0L", "Pint", "CUP",
			"I.C. Cone", "1.5L", "800mL", "475mL", "200mL");
	
	private final static List<String> FLAVORS_SORTING = Arrays.asList("Regular Ice Cream",
			"Special/Premium Ice Cream",
			"Sherbet");

	private final static List<String> CONES_SIZE_NAMES = Arrays.asList("BOX", "PACK");

	private final static List<String> OTHERS_SIZE_NAMES = Arrays.asList("PC","BOX", "PACK");

	private final static Ordering<String> SIZE_NAME_ORDERING = Ordering.explicit(SIZE_NAMES);
	
	private final static Ordering<String> CONE_SIZE_NAME_ORDERING = Ordering.explicit(CONES_SIZE_NAMES);
	
	private final static Ordering<String> OTHER_SIZE_NAME_ORDERING = Ordering.explicit(OTHERS_SIZE_NAMES);
	
	private final static Ordering<String> CATEGORY_NAME_ORDERING = Ordering.explicit(FLAVORS_SORTING);

	public static final NumberFormat getFormatter() {

		Locale locale = new Locale("en", "PH");
		NumberFormat currencyInstanceFormat = NumberFormat.getCurrencyInstance(locale);
		currencyInstanceFormat.setMinimumFractionDigits(2);
		currencyInstanceFormat.setMaximumFractionDigits(2);

		return currencyInstanceFormat;
	}

	public static final boolean isChecker(AppUser user) {
		return Role.Checker.name().equalsIgnoreCase(user.getRole());
	}

	public static final boolean isSales(AppUser user) {
		return Role.Sales.name().equalsIgnoreCase(user.getRole());
	}

	public static final boolean isSuperUser(AppUser user) {
		return Role.Superuser.name().equalsIgnoreCase(user.getRole());

	}
	
	public static final boolean isAccounting(AppUser user) {
		return Role.Accounting.name().equalsIgnoreCase(user.getRole());

	}

	public static final boolean isAdmin(AppUser user) {
		return Role.Admin.name().equalsIgnoreCase(user.getRole());
	}

	public static final boolean isOrderStatusEquals(String currentOrderStatus, OrderStatus orderStatus) {
		return orderStatus.getOrderStatusName().equals(currentOrderStatus);
	}

	public static final boolean isRelativeOrCompanyOwned(CustomerTag customerTag) {
		return COMPANY_OWNED_TAGS.contains(customerTag.getCustomerTagName());
	}

	public static final boolean isRelativeOrCompanyOwned(String customerTag) {
		return COMPANY_OWNED_TAGS.contains(customerTag);
	}

	public static final String formatDate(LocalDateTime localDateTime) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		String formatDateTime = localDateTime.format(formatter);

		return formatDateTime;
	}

	public static String formatDateWithHours(LocalDateTime localDateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		String formatDateTime = localDateTime.format(formatter);

		return formatDateTime;
	}

	public static Comparator<Size> sizeComparator = new Comparator<Size>() {

		@Override
		public int compare(Size arg0, Size arg1) {
			return SIZE_NAME_ORDERING.compare(arg0.getSizeName(), arg1.getSizeName());
		}
	};
	
	public static Comparator<Size> coneSizeComparator = new Comparator<Size>() {

		@Override
		public int compare(Size arg0, Size arg1) {
			return CONE_SIZE_NAME_ORDERING.compare(arg0.getSizeName(), arg1.getSizeName());
		}
	};
	
	public static Comparator<Size> otherSizeComparator = new Comparator<Size>() {

		@Override
		public int compare(Size arg0, Size arg1) {
			return OTHER_SIZE_NAME_ORDERING.compare(arg0.getSizeName(), arg1.getSizeName());
		}
	};

	public static Comparator<ItemStock> itemStockComparator = new Comparator<ItemStock>() {

		@Override
		public int compare(ItemStock arg0, ItemStock arg1) {
			return SIZE_NAME_ORDERING.compare(arg0.getSize().getSizeName(), arg1.getSize().getSizeName());
		}
	};

	public static Comparator<ItemStock> coneSizeItemStockComparator = new Comparator<ItemStock>() {

		@Override
		public int compare(ItemStock arg0, ItemStock arg1) {
			return CONE_SIZE_NAME_ORDERING.compare(arg0.getSize().getSizeName(), arg1.getSize().getSizeName());
		}
	};

	public static Comparator<ItemStock> otherSizeItemStockComparator = new Comparator<ItemStock>() {

		@Override
		public int compare(ItemStock arg0, ItemStock arg1) {
			return OTHER_SIZE_NAME_ORDERING.compare(arg0.getSize().getSizeName(), arg1.getSize().getSizeName());
		}
	};
	
	public static Comparator<String> categoryNameComparator = new Comparator<String>() {

		@Override
		public int compare(String arg0, String arg1) {
			return CATEGORY_NAME_ORDERING.compare(arg0, arg1);
		}
	};

	public static void setVisibility(boolean show, Component...components ) {
		for (Component component : components) {
			component.setVisible(show);
		}
		
	}
	
	
	public static void sort(Category category, List<Size> size) {
		
		if (Categories.Flavors.name().equals(category.getCategoryType())){
			Collections.sort(size, sizeComparator);
		}
		
		if (Categories.Cones.name().equals(category.getCategoryType())){
			Collections.sort(size, coneSizeComparator);	
		}

		if (Categories.Others.name().equals(category.getCategoryType())){
			Collections.sort(size, otherSizeComparator);
		}
		
	}
	
	public static Map<Integer, List<ProductPrice>> createProductPricePerSizeId(Product product, CustomerTag customerTag) {
		return product.getProductPrices().stream()
				.filter(productPrice -> {
					return productPrice.getCustomerTagId() == customerTag.getId();
				}).collect(Collectors.groupingBy(productPrice -> productPrice.getSize().getId()));
	}

	public static Map<String, ItemStock> createSizeMap(Product product) {
		return product.getItemStock().stream().collect(Collectors.toMap(e -> e.getSize().getSizeName(), Function.identity()));
	}
	
	public static boolean isRelative(CustomerTag customerTag) {
		return customerTag.getCustomerTagName().equals(CustomerTags.RELATIVE_OWNED.getCustomerTagName());
	}
	
	public static boolean isCompanyOwned(CustomerTag customerTag) {
		return customerTag.getCustomerTagName().equals(CustomerTags.COMPANY_OWNED.getCustomerTagName())
				|| customerTag.getCustomerTagName().equals(CustomerTags.MAIN_STORE.getCustomerTagName());
	}
	
	public static boolean isDealer(CustomerTag customerTag) {
		return customerTag.getCustomerTagName().equals(CustomerTags.DEALER.getCustomerTagName());
	}
	
	public static boolean isReseller(CustomerTag customerTag) {
		return customerTag.getCustomerTagName().equals(CustomerTags.RESELLER.getCustomerTagName());
	}
	
	public static boolean isPartner(CustomerTag customerTag) {
		return customerTag.getCustomerTagName().equals(CustomerTags.PARTNER.getCustomerTagName());
	}
	
	public static BigDecimal getTotalPrice(OrderItems orderItem, CustomerTag customerTag) {
		if (PfdiUtil.isCompanyOwned(customerTag)) {
			System.out.println("SRP : "  +  orderItem.getProductSrp()  + " Q: " + orderItem.getQuantity() );
			return orderItem.getProductSrp().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
		} else if (PfdiUtil.isRelative(customerTag)) {
			return orderItem.getProductPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
		} else {
			return orderItem.getProductPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
		}
	}

	public static String getFullName(AppUser appUser) {
		final String firstName = appUser.getFirstName();
		final String lastName = appUser.getLastName();
		return firstName + " " + lastName;
	}

}
