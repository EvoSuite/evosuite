/**
 * 
 */
package com.examples.with.different.packagename;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author Gordon Fraser
 * 
 */
public class CurrentTimeViaCalendar2 {
	public long getCurrentTime(long time) {
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		long currentTime = calendar.getTimeInMillis();
		if (time == currentTime) {
			return currentTime;
		}

		return 0L;
	}
}
