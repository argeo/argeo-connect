package org.argeo.connect.payment.stripe;

import static org.argeo.connect.payment.stripe.StripeConstants.STRIPE_ACCOUNT_MANAGED;
import static org.argeo.connect.payment.stripe.StripeConstants.STRIPE_COUNTRY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Charge;
import com.stripe.model.ChargeCollection;

/**
 * Centralises call to the Stripe API
 */
public class StripeService {

	public StripeService() {
		Stripe.apiKey = "sk_test_rgydDhfOG9cu1y2N2PDKs0Pd";
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

	/** Does not work with account that have not yet been verified */
	public List<Charge> listCharges(String filter) throws StripeException {
		ChargeCollection charges = Charge.list(null);
		return charges.getData();
	}

}
