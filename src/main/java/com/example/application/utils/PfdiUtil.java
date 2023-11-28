package com.example.application.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
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
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.orders.OrderItems;
import com.example.application.data.entity.orders.Transaction;
import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.CustomerTag;
import com.example.application.data.entity.products.LocationTag;
import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.ProductPrice;
import com.example.application.data.entity.products.Size;
import com.example.application.data.entity.stock.Inventory;
import com.example.application.data.entity.stock.ItemStock;
import com.google.gwt.thirdparty.guava.common.collect.Ordering;
import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.vaadin.flow.component.Component;

public class PfdiUtil {

	private final static Set<String> COMPANY_OWNED_TAGS = Sets.newHashSet("Relative Owned", "Company Owned",
			"Main Store");

	// T/C, 3.4L, 1.9L, 1.0L, PINT, CUP, I.C. CONE, 1.5L, 800ML, 475ML, 200ML
	private final static List<String> SIZE_NAMES = Arrays.asList("T/C", "3.4L", "1.9L", "1.0L", "475mL", "200mL",
			"Pint", "Cup", "CUP", "Cups", "I.C. Cone", "1.5L", "800mL", "OTHERS", "OTHER_FLAVORS_SIZE");

	private final static List<String> FLAVORS_SORTING = Arrays.asList("Regular Ice Cream", "Special/Premium Ice Cream", "Seasonal Ice Cream",
			"Sherbet", "OTHER_FLAVORS");

	private final static List<String> CATEGORY_TYPE_SORTING = Arrays.asList("Flavors", "Cones", "Others");

	private final static List<String> CONES_SIZE_NAMES = Arrays.asList("BOX", "PACK", "OTHERS", "OTHER_CONES_SIZE");

	private final static List<String> OTHERS_SIZE_NAMES = Arrays.asList("PACK - OTHERS", "PACK", "PC", "BOX", "OTHERS",
			"OTHER_OTHERS_SIZE");

	private final static List<String> ALL_SIZE_NAMES = Arrays.asList("T/C", "3.4L", "1.9L", "1.0L", "Pint", "CUP",
			"I.C. Cone", "1.5L", "800mL", "475mL", "200mL", "BOX", "PACK", "PC", "PACK - OTHERS", "OTHERS");

	private final static Ordering<String> SIZE_NAME_ORDERING = Ordering.explicit(SIZE_NAMES)
			.onResultOf(lang -> SIZE_NAMES.contains(lang) ? lang : "OTHER_FLAVORS_SIZE");

	private final static Ordering<String> CONE_SIZE_NAME_ORDERING = Ordering.explicit(CONES_SIZE_NAMES)
			.onResultOf(lang -> CONES_SIZE_NAMES.contains(lang) ? lang : "OTHER_CONES_SIZE");

	private final static Ordering<String> OTHER_SIZE_NAME_ORDERING = Ordering.explicit(OTHERS_SIZE_NAMES)
			.onResultOf(lang -> OTHERS_SIZE_NAMES.contains(lang) ? lang : "OTHER_OTHERS_SIZE");

	private final static Ordering<String> CATEGORY_NAME_ORDERING = Ordering.explicit(FLAVORS_SORTING)
			.onResultOf(lang -> FLAVORS_SORTING.contains(lang) ? lang : "OTHER_FLAVORS");

	private final static Ordering<String> CATEGORY_TYPE_ORDERING = Ordering.explicit(CATEGORY_TYPE_SORTING)
			.onResultOf(lang -> CATEGORY_TYPE_SORTING.contains(lang) ? lang : "OTHER_CATEGORY");

	private final static Ordering<String> ALL_SIZE_NAME_ORDERING = Ordering.explicit(ALL_SIZE_NAMES)
			.onResultOf(lang -> ALL_SIZE_NAMES.contains(lang) ? lang : "OTHERS");

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

	public static final String formatDate(LocalDate localDate) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		String formatDateTime = localDate.format(formatter);

		return formatDateTime;
	}

	public static String formatDateWithHours(LocalDateTime localDateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		String formatDateTime = localDateTime.format(formatter);

		return formatDateTime;
	}
	
	public static String formatTime(LocalDateTime localDateTime) {
		
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

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
			int order = CONE_SIZE_NAME_ORDERING.compare(arg0.getSizeName(), arg1.getSizeName());
			return order;
		}
	};

	public static Comparator<Size> otherSizeComparator = new Comparator<Size>() {

		@Override
		public int compare(Size arg0, Size arg1) {
			return OTHER_SIZE_NAME_ORDERING.compare(arg0.getSizeName(), arg1.getSizeName());
		}
	};

	public static Comparator<Inventory> itemStockComparator = new Comparator<Inventory>() {

		@Override
		public int compare(Inventory arg0, Inventory arg1) {
			return ALL_SIZE_NAME_ORDERING.compare(arg0.getName(), arg0.getName());
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
	
	public static Comparator<String> categoryNameThenSizeComparator = new Comparator<String>() {

		@Override
		public int compare(String arg0, String arg1) {
			return CATEGORY_NAME_ORDERING.compare(arg0, arg1);
		}
	};

	public static Comparator<Category> categoryTypeComparator = new Comparator<Category>() {

		@Override
		public int compare(Category arg0, Category arg1) {
			return CATEGORY_TYPE_ORDERING.compare(arg0.getCategoryType(), arg1.getCategoryType());
		}
	};

	public static Comparator<String> categoryNameInventortComparator = new Comparator<String>() {

		@Override
		public int compare(String arg0, String arg1) {
			return CATEGORY_NAME_ORDERING.compare(arg0, arg1);
		}
	};

	public static void setVisibility(boolean show, Component... components) {
		for (Component component : components) {
			component.setVisible(show);
		}

	}

	public static void sort(Category category, List<Size> size) {

		if (Categories.Flavors.name().equals(category.getCategoryType())) {
			Collections.sort(size, sizeComparator);
		}

		if (Categories.Cones.name().equals(category.getCategoryType())) {
			Collections.sort(size, coneSizeComparator);
		}

		if (Categories.Others.name().equals(category.getCategoryType())) {
			Collections.sort(size, otherSizeComparator);
		}

	}

	public static Map<Integer, List<ProductPrice>> createProductPricePerSizeId(Product product,
			CustomerTag customerTag) {
		return product.getProductPrices().stream().filter(productPrice -> {
			return productPrice.getCustomerTagId() == customerTag.getId();
		}).collect(Collectors.groupingBy(productPrice -> productPrice.getSize().getId()));
	}

	public static Map<String, ItemStock> createSizeMap(Product product) {
		return product.getItemStock().stream()
				.collect(Collectors.toMap(e -> e.getSize().getSizeName(), Function.identity()));
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
			if (orderItem.getProductSrp() == null) {
				return BigDecimal.ZERO;
			}

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

	public static OrderStatus getStatus(String status) {
		for (OrderStatus orderStatus : OrderStatus.values()) {
			if (orderStatus.getOrderStatusName().equalsIgnoreCase(status)) {
				return orderStatus;
			}
		}
		throw new IllegalArgumentException(String.valueOf(status));
	}

	public static boolean isProductValid(Product product, LocalDate startDate, LocalDate endDate) {

		LocalDate now = LocalDate.now();
		boolean isWithinDate = startDate.isBefore(now) && endDate.isAfter(now);
		return product.getActiveStatus() && isWithinDate;
	}

	public static String getCurrencyAmount(BigDecimal amount) {
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		String amountString = formatter.format(amount);

		return amountString;

	}

	public static final String getSrOrDrString(Order order) {
		if (PfdiUtil.isRelativeOrCompanyOwned(order.getCustomer().getCustomerTagId())) {
			if (order.getStockTransferId() != null) {
				return order.getStockTransferId().toString();
			}
			return "";
		} else {
			if (order.getDeliveryReceiptId() != null) {
				return order.getDeliveryReceiptId().toString();
			}
			return "";
		}
	}

	public static final String getTagString(Customer customer) {
		CustomerTag customerTag = customer.getCustomerTagId();
		LocationTag locationTag = customer.getLocationTagId();
		return locationTag.getLocationTagName() + " - " + customerTag.getCustomerTagName();
	}

	public static final Comparator<Transaction> getLocalDateComparator() {

		return new Comparator<Transaction>() {

			@Override
			public int compare(Transaction t1, Transaction t2) {
				LocalDateTime o1 = t1.getDate();
				LocalDateTime o2 = t2.getDate();
				int result = o1.toLocalDate().compareTo(o2.toLocalDate()); // Consider only the date portion first.

				result = ((-1) * result); // Flip the positive/negative sign of the int, to get ascending order. Or more
											// simply: `= - result ;`.
				if (0 == result) // If dates are equal, look at the time-of-day.
				{
					System.out.println("reversing ");
					result = o1.toLocalTime().compareTo(o2.toLocalTime());
				}
				return result;
			}
		};

	}

	public static void sortByNameThenSize(List<OrderItems> cones) {

		Collections.sort(cones, (o1, o2) -> {
			if (o1.getItemInventory().getProduct().getProductName()
					.compareTo(o2.getItemInventory().getProduct().getProductName()) == 0) {
				return o1.getItemInventory().getSize().getSizeOrder().compareTo(o2.getItemInventory().getSize().getSizeOrder());
			} else {
				return o1.getItemInventory().getProduct().getProductName()
						.compareTo(o2.getItemInventory().getProduct().getProductName());
			}
		});

	}
	
	public static void sortBySizeOrder(List<OrderItems> items) {

		Collections.sort(items, (o1, o2) -> {

			return o1.getItemInventory().getSize().getSizeOrder().compareTo(o2.getItemInventory().getSize().getSizeOrder());
		});

	}
	
	
	public static void sortSizeByOrderId(List<Size> sizes) {

		Collections.sort(sizes, (o1, o2) -> {

			return o1.getSizeOrder().compareTo(o2.getSizeOrder());
		});

	}

}
