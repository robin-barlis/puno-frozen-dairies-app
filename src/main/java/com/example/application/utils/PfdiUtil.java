package com.example.application.utils;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.example.application.data.OrderStatus;
import com.example.application.data.Role;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.products.CustomerTag;
import com.example.application.data.entity.products.Size;
import com.example.application.data.entity.stock.ItemStock;
import com.google.gwt.thirdparty.guava.common.collect.Ordering;
import com.google.gwt.thirdparty.guava.common.collect.Sets;

public class PfdiUtil {

	private final static Set<String> COMPANY_OWNED_TAGS = Sets.newHashSet("Relative Owned", "Company Owned", "Main Store");

	private final static List<String> SIZE_NAMES = Arrays.asList("T/C", "3.4L", "1.9L", "1.0L", "Pint", "CUP",
			"I.C. Cone", "1.5L", "800mL", "475mL", "200mL");

	private final static List<String> CONES_SIZE_NAMES = Arrays.asList("BOX", "PACK");

	private final static List<String> OTHERS_SIZE_NAMES = Arrays.asList("PC", "PACK");

	private final static Ordering<String> SIZE_NAME_ORDERING = Ordering.explicit(SIZE_NAMES);
	
	private final static Ordering<String> CONE_SIZE_NAME_ORDERING = Ordering.explicit(CONES_SIZE_NAMES);
	
	private final static Ordering<String> OTHER_SIZE_NAME_ORDERING = Ordering.explicit(OTHERS_SIZE_NAMES);

	public static final NumberFormat getFormatter() {

		NumberFormat currencyInstanceFormat = NumberFormat.getCurrencyInstance();
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

}
