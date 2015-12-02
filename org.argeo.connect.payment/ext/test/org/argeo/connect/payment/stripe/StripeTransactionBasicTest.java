package org.argeo.connect.payment.stripe;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Test the basic communication with stripe */
public class StripeTransactionBasicTest extends TestCase {
	private final static Log log = LogFactory.getLog(StripeTransactionBasicTest.class);

	public void testIt() throws Exception {
		log.trace("Basic JUnit test. Insure tests are correctly configured");
	}
}