package org.argeo.connect.people.imports;

import org.argeo.support.junit.AbstractSpringTestCase;

/** Simple test to verify that the ez-vCard library is there **/
public class EzvcardTest extends AbstractSpringTestCase {
	// private final static Log log = LogFactory.getLog(EzvcardTest.class);

	private final static String PATH_TO_FILES = "org/argeo/connect/people/imports/";

	public void testCanonical() throws Exception {
		String text = "BEGIN:vcard\r\n" + "VERSION:3.0\r\n"
				+ "N:House;Gregory;;Dr;MD\r\n"
				+ "FN:Dr. Gregory House M.D.\r\n" + "END:vcard\r\n";
		// VCard vcard = Ezvcard.parse(text).first();
		// log.info("Name : " + vcard.getFormattedName().getValue());
	}

	// Temporary remove dependency to ez-vCard

	// public void testIt() throws Exception {
	// Resource resource = new ClassPathResource(PATH_TO_FILES
	// + "contacts.vcf");
	// VCard vcard = null;
	// InputStream is = null;
	// try {
	// is = resource.getInputStream();
	// vcard = Ezvcard.parse(is).first();
	// log.info("Name : " + vcard.getFormattedName().getValue());
	// } finally {
	// IOUtils.closeQuietly(is);
	// }
	// }
}