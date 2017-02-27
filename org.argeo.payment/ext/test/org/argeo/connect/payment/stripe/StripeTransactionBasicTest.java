package org.argeo.connect.payment.stripe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.stripe.model.Account;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;

/**
 * Test the basic communication with stripe
 * 
 * WARNING these test are not meant to be run in a normal build process, thus
 * pay attention to leave the doTest flag set to false
 */
public class StripeTransactionBasicTest extends TestCase implements
		StripeConstants {
	private final static Log log = LogFactory
			.getLog(StripeTransactionBasicTest.class);

	boolean doTest = false;

	private StripeService stripeService;

	protected void setUp() throws Exception {
		stripeService = new StripeService();
	}

	public void testManagedAccount() throws Exception {
		// This test will fail until the platform corresponding stripe account
		// has been registered
		boolean doIt = false;
		if (!doTest || !doIt)
			return;

		// Create a managed account
		StripeService ss = new StripeService();
		String accId = ss.createManagedAccount("FR");
		Account account = Account.retrieve(accId, (RequestOptions) null);
		Map<String, Object> tosAcceptanceParams = new HashMap<String, Object>();
		long date = (long) System.currentTimeMillis() / 1000L;
		tosAcceptanceParams.put(STRIPE_DATE, date);
		// FIXME Assumes you're not using a proxy
		tosAcceptanceParams.put(STRIPE_IP, "8.8.8.8");
		Map<String, Object> accountParams = new HashMap<String, Object>();
		accountParams.put(STRIPE_TOS_ACCEPTANCE, tosAcceptanceParams);
		account.update(accountParams);
	}

	/** from https://stripe.com/docs/api?lang=java */
	public void testCharge() throws Exception {
		if (!doTest)
			return;

		Map<String, Object> chargeParams = new HashMap<String, Object>();
		RequestOptions options = RequestOptions.builder()
				.setIdempotencyKey("1NxN24GybhwfA1G5").build();

		// Use a token obtained with Stripe.js
		// chargeParams.put("source", "tok_175Gk1DD4YBBbu5ao8DFl7W6");

		// Use a card
		Map<String, Object> cardMap = new HashMap<String, Object>();
		cardMap.put(STRIPE_CARD_NB, "4242424242424242");
		cardMap.put(STRIPE_CARD_EXP_MONTH, 11);
		cardMap.put(STRIPE_CARD_EXP_YEAR, 2018);
		cardMap.put(STRIPE_CARD_CVC, "123");
		cardMap.put(STRIPE_COUNTRY, "FR");
		chargeParams.put(STRIPE_CHARGE_CARD, cardMap);

		// Business info
		chargeParams.put(STRIPE_CHARGE_AMOUNT, 1000);
		chargeParams.put(STRIPE_CURRENCY, "eur");
		chargeParams.put(STRIPE_DESC, "Charge for test@example.com");

		// Add a reference to a business ID
		String uid = UUID.randomUUID().toString();
		Map<String, Object> metadataMap = new HashMap<String, Object>();
		metadataMap.put("businessId", uid);
		chargeParams.put(STRIPE_METADATA, metadataMap);

		Charge charge = Charge.create(chargeParams, options);
		String chargeId = charge.getId();

		Charge charge2 = Charge.retrieve(chargeId);
		assertEquals(chargeId, charge2.getId());

		List<Charge> charges = stripeService.listCharges(null);
		for (Charge currCharge : charges) {
			log.debug(Charge.PRETTY_PRINT_GSON.toJson(currCharge));
		}
	}
}