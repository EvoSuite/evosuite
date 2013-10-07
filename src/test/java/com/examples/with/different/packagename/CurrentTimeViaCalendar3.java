/**
 * 
 */
package com.examples.with.different.packagename;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Gordon Fraser
 * 
 */
public class CurrentTimeViaCalendar3 {
	public long getCurrentTime(long time) {
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.ENGLISH);
		long currentTime = calendar.getTimeInMillis();
		if (time == currentTime) {
			return currentTime;
		}

		return 0L;
	}
}
