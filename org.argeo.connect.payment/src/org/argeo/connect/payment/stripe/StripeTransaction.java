package org.argeo.connect.payment.stripe;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.payment.Counterparty;
import org.argeo.connect.payment.Transaction;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;

public class StripeTransaction implements Transaction {
	private final static Log log = LogFactory.getLog(StripeTransaction.class);

	@Override
	public Object getBusinessId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Counterparty getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Counterparty getTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	/** from https://stripe.com/docs/api?lang=java */
	public void test() {
		log.debug("Basic stripe test");
		try {
			Stripe.apiKey = "sk_test_rgydDhfOG9cu1y2N2PDKs0Pd";

			Map<String, Object> chargeParams = new HashMap<String, Object>();
			chargeParams.put("amount", 400);
			chargeParams.put("currency", "eur");
			chargeParams.put("source", "tok_175Gk1DD4YBBbu5ao8DFl7W6"); // obtained
																		// with
																		// Stripe.js
			chargeParams.put("description", "Charge for test@example.com");

			RequestOptions options = RequestOptions.builder()
					.setIdempotencyKey("5NxN9SYyYhwfPCG5").build();

			Charge.create(chargeParams, options);
		} catch (StripeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
