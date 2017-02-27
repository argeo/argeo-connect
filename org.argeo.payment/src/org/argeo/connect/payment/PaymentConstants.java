package org.argeo.connect.payment;

public interface PaymentConstants {

	/* PAYMENT TAGS NAMES */
	public final static String PAYMENT_AMOUNT = "amount";
	public final static String PAYMENT_CURRENCY = "currency";
	public final static String PAYMENT_DESC = "description";
	public final static String PAYMENT_METADATA = "metadata";
	public final static String RECEIPT_EMAIL = " receipt_email";

	// Card
	public final static String CARD_NAME = "name";
	public final static String CARD_NB = "number";
	public final static String CARD_EXP_MONTH = "exp_month";
	public final static String CARD_EXP_YEAR = "exp_year";
	public final static String CARD_CVC = "cvc";
	public final static String CARD_COUNTRY = "country";
}