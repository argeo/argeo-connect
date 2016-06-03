package org.argeo.connect.payment.stripe;

import static org.argeo.connect.payment.stripe.StripeConstants.STRIPE_ACCOUNT_MANAGED;
import static org.argeo.connect.payment.stripe.StripeConstants.STRIPE_COUNTRY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Charge;
import com.stripe.model.ChargeCollection;
import com.stripe.net.RequestOptions;

/** Centralises call to the Stripe API */
public class StripeService {
	private final static Log log = LogFactory.getLog(StripeService.class);

	public StripeService() {
		String apiKey = System.getProperty(StripeConstants.STRIPE_API_KEY_PROP);
		if (apiKey != null)
			Stripe.apiKey = apiKey;
		else
			log.warn("No stripe API key found, "
					+ "the key must be set externally or "
					+ "the following calls to Stripe API will fail");
	}

	/** Does not work with account that have not yet been verified */
	public String createManagedAccount(String countryCode)
			throws StripeException {
		Map<String, Object> accountParams = new HashMap<String, Object>();
		accountParams.put(STRIPE_COUNTRY, countryCode);
		accountParams.put(STRIPE_ACCOUNT_MANAGED, true);
		Account newAccount = Account.create(accountParams);
		return newAccount.getId();
	}

	/** Retrieves all charges for the current connected account */
	public List<Charge> listCharges(String filter) throws StripeException {
		ChargeCollection charges = Charge.list(null);
		return charges.getData();
	}

	/** */
	public String processCharge(Map<String, Object> chargeParams,
			String idemPotencyKey) throws StripeException {
		RequestOptions options = RequestOptions.builder()
				.setIdempotencyKey(idemPotencyKey).build();
		Charge charge = Charge.create(chargeParams, options);
		String chargeId = charge.getId();
		return chargeId;
	}
}