/**
 * 
 */
package com.examples.with.different.packagename;

import java.util.Calendar;
import java.util.Locale;

/**
 * @author Gordon Fraser
 * 
 */
public class CurrentTimeViaCalendar1 {
	public long getCurrentTime(long time) {
		Calendar calendar = Calendar.getInstance(Locale.CANADA);
		long currentTime = calendar.getTimeInMillis();
		if (time == currentTime) {
			return currentTime;
		}

		return 0L;
	}
}
