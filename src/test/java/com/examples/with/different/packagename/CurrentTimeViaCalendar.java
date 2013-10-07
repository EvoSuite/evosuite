/**
 * 
 */
package com.examples.with.different.packagename;

import java.util.Calendar;

/**
 * @author Gordon Fraser
 * 
 */
public class CurrentTimeViaCalendar {

	public long getCurrentTime(long time) {
		Calendar calendar = Calendar.getInstance();
		long currentTime = calendar.getTimeInMillis();
		if (time == currentTime) {
			return currentTime;
		}

		return 0L;
	}
}
