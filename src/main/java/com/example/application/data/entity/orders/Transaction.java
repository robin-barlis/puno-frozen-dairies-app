package com.example.application.data.entity.orders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.example.application.data.PaymentMode;
import com.example.application.data.entity.payment.Payment;

public class Transaction {
	
	LocalDateTime date;
	Integer soNumber;
	Integer siNumber;
	LocalDate chequeDueDate;
	String particulars;
	BigDecimal amountGross;
	BigDecimal discount;
	BigDecimal netCollection;
	String paymentMode;
	
	public Transaction(Payment payment) {
		this.date = payment.getPaymentDate().atTime(LocalTime.MAX);
		this.siNumber = payment.getOrderId().getInvoiceId();
		PaymentMode mode = PaymentMode.valueOf(payment.getPaymentMode());
		if (PaymentMode.CHEQUE == mode) {
			this.chequeDueDate = payment.getChequePaymentDetails().getChequeDueDate();

			this.particulars = payment.getChequePaymentDetails().getChequeNumber();
		}
		
		if (PaymentMode.ONLINE_REMITTANCE == mode) {
			this.particulars = payment.getBankRemittanceDetails().getAccountNumber();
		}
		this.paymentMode = PaymentMode.valueOf(payment.getPaymentMode()).getName();
		this.netCollection = payment.getAmount();
	}
	
	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public Integer getSoNumber() {
		return soNumber;
	}

	public Integer getSiNumber() {
		return siNumber;
	}

	public LocalDate getChequeDueDate() {
		return chequeDueDate;
	}

	public String getParticulars() {
		return particulars;
	}

	public BigDecimal getAmountGross() {
		return amountGross;
	}

	public BigDecimal getDiscount() {
		return discount;
	}

	public BigDecimal getNetCollection() {
		return netCollection;
	}

	public Transaction(Order order) {
		this.date = order.getCreationDate();
		this.soNumber = order.getStockOrderNumber();
		this.amountGross = order.getAmountDue();
		this.discount = order.getDiscount();
		
	}
	
	

}
