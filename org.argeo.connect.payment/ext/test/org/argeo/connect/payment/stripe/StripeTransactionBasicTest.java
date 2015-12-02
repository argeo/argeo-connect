package org.argeo.connect.payment.stripe;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Test the basic communication with stripe */
public class StripeTransactionBasicTest extends TestCase {
	private final static Log log = LogFactory
			.getLog(StripeTransactionBasicTest.class);

	public void testIt() throws Exception {
		// does not create a real test transaction at each build
		// (requires to be on-line)
		boolean doIt = false;
		if (doIt) {
			StripeTransaction st = new StripeTransaction();
			st.test();
			log.debug("Successfully tested to stripe");
		}
	}
}