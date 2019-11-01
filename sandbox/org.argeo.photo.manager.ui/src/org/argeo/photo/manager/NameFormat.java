package org.argeo.photo.manager;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

public class NameFormat extends Format {
	public final static long serialVersionUID = System.currentTimeMillis();

	public StringBuffer format(Object obj, StringBuffer toAppendTo,
			FieldPosition pos) {
		toAppendTo.append(obj.toString());
		stdOut("Field position: " + pos);
		return toAppendTo;
	}

	public Object parseObject(String source, ParsePosition pos) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NameFormat nFormat = new NameFormat();
		//Object[] arr = { new Integer(5) };
		stdOut(nFormat.format("Text {0}"));

	}

	public static void stdOut(Object o) {
		System.out.println(o);
	}

}
