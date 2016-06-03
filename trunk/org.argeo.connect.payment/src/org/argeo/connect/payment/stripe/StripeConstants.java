package org.argeo.connect.payment.stripe;

public interface StripeConstants {

	public final static String STRIPE_API_KEY_PROP = "connect.payment.stripe.apiKey";
	
	/* STRIPE TAGS NAMES */
	
	// Various
	public final static String STRIPE_DESC = "description";
	public final static String STRIPE_METADATA = "metadata";
	public final static String STRIPE_COUNTRY = "country";
	public final static String STRIPE_CURRENCY = "currency";
	public final static String STRIPE_DATE = "date";
	public final static String STRIPE_IP = "ip";
	public final static String STRIPE_TOS_ACCEPTANCE = "tos_acceptance";

	// Account
	public final static String STRIPE_ACCOUNT_MANAGED = "managed";

	// Card
	public final static String STRIPE_CARD_NB = "number";
	public final static String STRIPE_CARD_EXP_MONTH = "exp_month";
	public final static String STRIPE_CARD_EXP_YEAR = "exp_year";
	public final static String STRIPE_CARD_CVC = "cvc";

	// Charge
	public final static String STRIPE_CHARGE_AMOUNT = "amount";
	public final static String STRIPE_CHARGE_CARD = "card";
	public final static String STRIPE_CHARGE_APP_FEE = "application_fee";
	public final static String STRIPE_CHARGE_DEST = "destination";

}
