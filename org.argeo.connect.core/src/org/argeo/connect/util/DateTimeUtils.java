package org.argeo.connect.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

/** Utilities around date and time management. */
public class DateTimeUtils {
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

	/* LENGTH AND DURATION MANAGEMENT */
	/** returns corresponding hours for a HH:MM:SS representation */
	public static long getHoursFromLength(long lengthInSeconds) {
		return (lengthInSeconds / (60 * 60)) % 60;
	}

	/** returns corresponding minutes for a HH:MM:SS representation */
	public static long getMinutesFromLength(long lengthInSeconds) {
		return (lengthInSeconds / 60) % 60;
	}

	/** returns corresponding seconds for a HH:MM:SS representation */
	public static long getSecondsFromLength(long lengthInSeconds) {
		return lengthInSeconds % 60;
	}

	/** returns the length in second of a HH:MM:SS representation */
	public static long getLengthFromHMS(int hours, int min, int secs) {
		return 60 * 60 * hours + 60 * min + secs;
	}

	/** Approximate the length in seconds in minute, round to the closest minute */
	public static long roundSecondsToMinutes(long lengthInSeconds) {
		long grounded = (lengthInSeconds / 60);
		if (getSecondsFromLength(lengthInSeconds) > 30)
			grounded++;
		return grounded;
	}

	/** format a duration in second using a hh:mm:ss pattern */
	public static String getLengthFormattedAsString(long lengthInSeconds) {
		return String.format("%02d:%02d:%02d",
				getHoursFromLength(lengthInSeconds),
				getMinutesFromLength(lengthInSeconds),
				getSecondsFromLength(lengthInSeconds));
	}

	private DateTimeUtils() {
	}
}
