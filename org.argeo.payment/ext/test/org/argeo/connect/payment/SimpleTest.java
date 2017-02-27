package org.argeo.connect.payment;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Insure the Junit tests run seamlessly */
public class SimpleTest extends TestCase {
	private final static Log log = LogFactory.getLog(SimpleTest.class);

	public void testIt() throws Exception {
		log.trace("Basic JUnit test. Insure tests are correctly configured");
	}
}