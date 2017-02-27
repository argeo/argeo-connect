package org.argeo.connect.payment;

public class PaymentUtils {

	private final static String cardValidationPattern = //
	"^(?:4[0-9]{12}(?:[0-9]{3})?" // Visa
			+ "|  5[1-5][0-9]{14}" // MasterCard
			+ "|  3[47][0-9]{13}" // American Express
			+ ")$";

	public static void checkProperty(String propertyName, String value)
			throws PaymentException {

	}

}
