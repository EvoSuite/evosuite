/**
 * 
 */
package org.evosuite.runtime;

import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Gordon Fraser
 * 
 */
public class Calendar {

	public static java.util.Calendar getCalendar() {
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		return cal;
	}

	public static java.util.Calendar getCalendar(Locale l) {
		java.util.Calendar cal = java.util.Calendar.getInstance(l);
		cal.setTimeInMillis(System.currentTimeMillis());
		return cal;
	}

	public static java.util.Calendar getCalendar(TimeZone t) {
		java.util.Calendar cal = java.util.Calendar.getInstance(t);
		cal.setTimeInMillis(System.currentTimeMillis());
		return cal;
	}

	public static java.util.Calendar getCalendar(TimeZone t, Locale l) {
		java.util.Calendar cal = java.util.Calendar.getInstance(t, l);
		cal.setTimeInMillis(System.currentTimeMillis());
		return cal;
	}

}
