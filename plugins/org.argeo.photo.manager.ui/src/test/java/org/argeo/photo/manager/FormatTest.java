package org.argeo.photo.manager;

import java.text.MessageFormat;
import java.util.Date;

import junit.framework.TestCase;

public class FormatTest extends TestCase {

	public void testParse() throws Exception {
		Long setId = new Long(5);// 0
		Long photoId = new Long(1);// 1
		Date date = new Date();// 2
		String label = "MySet";// 3

		Object[] arr = { setId, photoId, date, label };
		String formatStr = "{0,number,0000}-{2,date,yymm}-{3}-{1,number,000}.jpg";

		String result = MessageFormat.format(formatStr, arr);

		System.out.println(result);

		MessageFormat format = new MessageFormat(formatStr);
		Object[] objs = format.parse(result);

		assertEquals(setId.intValue(), ((Long) objs[0]).intValue());
		assertEquals(photoId.intValue(), ((Long) objs[1]).intValue());
		assertTrue(objs[2] instanceof Date);
		assertEquals(label, ((String) objs[3]).toString());
	}
}
