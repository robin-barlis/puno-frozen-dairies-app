/*
 * DynamicReports - Free Java reporting library for creating reports dynamically
 *
 * Copyright (C) 2010 - 2018 Ricardo Mariaca and the Dynamic Reports Contributors
 *
 * This file is part of DynamicReports.
 *
 * DynamicReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DynamicReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DynamicReports. If not, see <http://www.gnu.org/licenses/>.
 */
package com.example.application.reports;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.template;

import java.awt.Color;
import java.util.Locale;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.orders.Order;
import com.example.application.utils.PfdiUtil;

import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter;
import net.sf.dynamicreports.report.builder.ReportTemplateBuilder;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.datatype.BigDecimalType;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import net.sf.dynamicreports.report.definition.ReportParameters;

public class Templates {
    /**
     * Constant <code>rootStyle</code>
     */
    public static final StyleBuilder rootStyle;
    /**
     * Constant <code>boldStyle</code>
     */
    public static final StyleBuilder boldStyle;
    /**
     * Constant <code>italicStyle</code>
     */
    public static final StyleBuilder italicStyle;
    /**
     * Constant <code>boldCenteredStyle</code>
     */
    public static final StyleBuilder boldCenteredStyle;
    /**
     * Constant <code>bold12CenteredStyle</code>
     */
    public static final StyleBuilder bold12CenteredStyle;
    /**
     * Constant <code>columnStyle</code>
     */
    public static final StyleBuilder columnStyle;
    
    /**
     * Constant <code>columnStyle</code>
     */
    public static final StyleBuilder flavorColumnStyle;
    /**
     * Constant <code>columnTitleStyle</code>
     */
    public static final StyleBuilder columnTitleStyle;
    /**
     * Constant <code>groupStyle</code>
     */
    public static final StyleBuilder groupStyle;
    /**
     * Constant <code>subtotalStyle</code>
     */
    public static final StyleBuilder subtotalStyle;

    /**
     * Constant <code>reportTemplate</code>
     */
    public static final ReportTemplateBuilder reportTemplate;
    /**
     * Constant <code>currencyType</code>
     */
    public static final CurrencyType currencyType;
    /**
     * Constant <code>footerComponent</code>
     */
    public static final ComponentBuilder<?, ?> footerComponent;
	public static final StyleBuilder columnHeaderStyle;
	public static final ReportTemplateBuilder reportTemplate2;
	public static final StyleBuilder columnStyleNoBorder;
	public static final StyleBuilder detailStyle;
	
	public static final StyleBuilder lineStyle;
	public static final StyleBuilder smallItalicFont;
	public static final StyleBuilder columnRight;

    static {
        rootStyle = stl.style().setPadding(2);
        boldStyle = stl.style(rootStyle).bold();
        italicStyle = stl.style(rootStyle).italic();
        
        smallItalicFont = stl.style(italicStyle).setFontSize(8);
        
        boldCenteredStyle = stl.style(boldStyle).setTextAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.MIDDLE);
        bold12CenteredStyle = stl.style(boldCenteredStyle).setFontSize(10);
        
        detailStyle = stl.style(boldCenteredStyle)
        		.setFontSize(10)
        		.setLeftPadding(4);

        columnStyle = stl.style(rootStyle)
				.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
				.setBottomBorder(stl.penThin().setLineColor(Color.LIGHT_GRAY));
        
        columnRight = stl.style(rootStyle)
				.setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
				;
        
        columnStyleNoBorder = stl.style(rootStyle)
				.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        
        columnHeaderStyle = stl.style(rootStyle)
  				.setTopBorder(stl.penThin())
  				.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);


        columnTitleStyle = stl.style(columnStyle)
				.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
				//.setBackgroundColor(Color.LIGHT_GRAY)
				.bold();
        groupStyle = stl.style(rootStyle).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        
        subtotalStyle = stl.style(boldStyle).setTopBorder(stl.pen1Point());

        flavorColumnStyle = stl.style(rootStyle)
				.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
	
				.setBottomBorder(stl.penThin().setLineColor(Color.LIGHT_GRAY));
        reportTemplate = template().setLocale(Locale.ENGLISH)
        						   .setGroupStyle(groupStyle)
                                   .setColumnTitleStyle(columnTitleStyle)
                                   .setColumnHeaderStyle(columnHeaderStyle)
                                   .setGroupTitleStyle(groupStyle)
                                   .setSubtotalStyle(subtotalStyle)
                                   .setColumnStyle(columnStyle);
        
        reportTemplate2 = template().setLocale(Locale.ENGLISH)
				.setGroupStyle(groupStyle)
                .setColumnTitleStyle(columnTitleStyle)
                .setColumnHeaderStyle(columnHeaderStyle)
                .setGroupTitleStyle(groupStyle)
                .setSubtotalStyle(subtotalStyle)
                .setColumnStyle(columnStyleNoBorder)
                .setDetailStyle(detailStyle);
        

        currencyType = new CurrencyType();
        
        lineStyle = stl.style().setLinePen(stl.penThin());
 
        footerComponent = cmp.pageXofY().setStyle(stl.style(boldCenteredStyle).setTopBorder(stl.pen1Point()));
        		

        
    }

    /**
     * <p>createCurrencyValueFormatter.</p>
     *
     * @param label a {@link java.lang.String} object.
     * @return a {@link net.sf.dynamicreports.examples.Templates.CurrencyValueFormatter} object.
     */
    public static CurrencyValueFormatter createCurrencyValueFormatter(String label) {
        return new CurrencyValueFormatter(label);
    }

    public static class CurrencyType extends BigDecimalType {
        private static final long serialVersionUID = 1L;

        @Override
        public String getPattern() {
            return "$ #,###.00";
        }
    }

    private static class CurrencyValueFormatter extends AbstractValueFormatter<String, Number> {
        private static final long serialVersionUID = 1L;

        private String label;

        public CurrencyValueFormatter(String label) {
            this.label = label;
        }

        @Override
        public String format(Number value, ReportParameters reportParameters) {
            return label + currencyType.valueToString(value, reportParameters.getLocale());
        }
    }
    
    
 
    public static ComponentBuilder<?, ?> createStockOrderDetailsComponent(Order order) {
        return cmp.horizontalList()
                  .add(cmp.text("S.O. No. " + order.getStockOrderNumber())
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT), 
                		  cmp.text(PfdiUtil.formatDate(order.getCreationDate()))
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT))
                  .newRow()
                  .add(cmp.verticalGap(20))
                  .newRow()
                  .add(cmp.text("Store Name: ")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                		  .setFixedWidth(80), 
                		  cmp.text(order.getCustomer().getStoreName())
                		  .setStyle(boldStyle))                  
                  .newRow()
                  .add(cmp.text("Address: ")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                		  .setFixedWidth(80), 
                		  cmp.text(order.getCustomer().getAddress())
                		  .setStyle(boldStyle))                  
                  .newRow()
                  .add(cmp.text("Owner Name: ")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                		  .setFixedWidth(80), 
                		  cmp.text(order.getCustomer().getOwnerName())
                		  .setStyle(boldStyle))
                  .add(cmp.verticalGap(40));
    }
    
    public static ComponentBuilder<?, ?> createStockTransferDetailsComponent(Order order) {
        return cmp.horizontalList()
                  .add(cmp.text("PUNO'S FROZEN DAIRIES INC")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT), 
                		  cmp.text("STOCK TRANSFER")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT))
                  .newRow()
                  .add(cmp.text("Victoria Subdivision, Bitas, Cabanatuan City")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT), 
                		  cmp.text(PfdiUtil.formatDate(order.getCreationDate()))
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT))                  
                  .newRow()
                  .add(cmp.text("Victoria Plant")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))                  
                  .newRow()
                  .add(cmp.verticalGap(20))
                  .newRow()
                  .add(cmp.text("Store Name: ")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                		  .setFixedWidth(80), 
                		  cmp.text(order.getCustomer().getStoreName())
                		  .setStyle(boldStyle))
                  .newRow()
                  .add(cmp.text("SO No.: ")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                		  .setFixedWidth(80), 
                		  cmp.text(order.getStockOrderNumber())
                		  .setStyle(boldStyle))
                  .add(cmp.verticalGap(40));
    }
    
    public static ComponentBuilder<?, ?> createDeliveryReceiptHeaderComponent(Order order) {
        return cmp.horizontalList()
                  .add(cmp.text("PUNO'S FROZEN DAIRIES INC")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER))
                  .newRow()
                  .add(cmp.text("Victoria Subdivision, Bitas, Cabanatuan City")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                		  .setStyle(smallItalicFont))                  
                  .newRow()
                  .add(cmp.text("Address: Victoria Subd., Bitas, Cabanatuan City                        Tel. No.: (044) 463-0818/464-8694/330-3676")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                		  .setStyle(smallItalicFont))                  
                  .newRow()
                  .add(cmp.text("Main Store: Mulawin 1, Bitas, Cabanatuan City                             Mobile No.: 0932-881-4249/0922-533-4987")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                		  .setStyle(smallItalicFont))                  
                  .newRow()
                  .add(cmp.text("VAT Reg. TIN: 006-745-463-000")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                		  .setStyle(smallItalicFont))
                  .newRow()
                  .add(cmp.text("Delivery Receipt Number " + order.getDeliveryReceiptId())
                   		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                		  .setFixedWidth(150)
                		  .setStyle(boldStyle),
                	   cmp.text(PfdiUtil.formatDate(order.getCreationDate()))
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)) 
                  .newRow()
                  .add(cmp.verticalGap(20))
                  .add(cmp.text("S.O. No. " + order.getStockOrderNumber())
                   		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))  
                  .newRow()
                  .add(cmp.text("Store Name: ")
                   		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                		  .setFixedWidth(80), 
                	   cmp.text(order.getCustomer().getStoreName())
                		  .setStyle(boldStyle))  
                  .newRow()
                  .add(cmp.text("Address:")
                   		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                		  .setFixedWidth(80), 
                	   cmp.text(order.getCustomer().getAddress())
                		  .setStyle(boldStyle))  
                  .newRow()
                  .add(cmp.text("Contact Number:")
                   		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                		  .setFixedWidth(80), 
                	   cmp.text(order.getCustomer().getContactNumber())
                		  .setStyle(boldStyle))  
                  .newRow();
    }



	public static ComponentBuilder<?, ?> createStockOrderDetailsFooterComponent(Order order, AppUser appUser) {
	     return cmp.horizontalList()
                 .add(cmp.text("NOTE: ____________________________________")
               		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))
                 .newRow()
                 .add(cmp.text("TIME RELEASED: ___________________________ ")
               		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT), 
               		  cmp.text("CHECKED BY: ___________________________ ")
               		  .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT))
                 .newRow()
                 .add(cmp.text("PREPARED BY: " + appUser.getFirstName() + " " + appUser.getLastName())
                  		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT), 
                  		  cmp.text("RECEIVED BY: ___________________________ ")
                  		  .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT))
                 
                 .add(cmp.verticalGap(40));
   
	}
	
	public static ComponentBuilder<?, ?> createStockTransferDetailsFooterComponent() {
	     return cmp.horizontalList()
                .add(cmp.text("_____________________________________")
                 		  .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT))
                .newRow()
                .add(cmp.text("Branch Personnel Signature Over Printed Name")
                 		  .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
                 		  .setStyle(smallItalicFont))
                
                .add(cmp.verticalGap(40));
  
	}

	public static ComponentBuilder<?, ?> stockOrderFooter() {
		
		return cmp.horizontalList()
                .add(cmp.line().setStyle(lineStyle))
                .newRow().newRow()
                .add(cmp.text("Received the above orders in good order and condition.")
               		  .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
               		  .setStyle(smallItalicFont));
	}
	
	public static ComponentBuilder<?, ?> createDeliveryReceiptDetailsFooterComponent() {
	     return cmp.horizontalList()
               .add(cmp.text("____________________________________________")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT))
               .newRow()
               .add(cmp.text("Customer Signature Over Printed Name")
                		  .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
                		  .setStyle(smallItalicFont))
               .newRow()
               .add(cmp.text("\"THIS DOCUMENT IS NOT VALID FOR CLAIM OF INPUT TAXES.\"")
             		  .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
             		  .setStyle(smallItalicFont))
               .newRow()
               .add(cmp.text("\"THIS DELIVERY RECEIPT SHALL BE VALID FOR FIVE (5) YEARS FROM THE DATE OF RELEASE.")
             		  .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
             		  .setStyle(smallItalicFont))
               .add(cmp.verticalGap(40));
 
	}
	
	
	
	
}