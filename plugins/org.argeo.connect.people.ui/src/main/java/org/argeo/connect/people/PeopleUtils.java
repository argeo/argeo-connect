package org.argeo.connect.people;

import java.util.Calendar;
import java.util.GregorianCalendar;

/** Utilities around people management. */
public class PeopleUtils {
	public static Integer computeAge(Calendar dateOfBirth) {
		Calendar now = new GregorianCalendar();
		int factor = 0;
		if (now.get(Calendar.DAY_OF_YEAR) < dateOfBirth
				.get(Calendar.DAY_OF_YEAR))
			factor = -1;
		Integer age = now.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR)
				+ factor;
		return age;
	}

	private PeopleUtils() {
	}
}
