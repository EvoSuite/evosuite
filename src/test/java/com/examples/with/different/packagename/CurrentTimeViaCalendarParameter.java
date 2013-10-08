/**
 * 
 */
package com.examples.with.different.packagename;

import java.util.Calendar;

/**
 * @author Gordon Fraser
 * 
 */
public class CurrentTimeViaCalendarParameter {
	public long getCurrentTime(Calendar calendar, long time) {
		long currentTime = calendar.getTimeInMillis();
		if (time == currentTime) {
			return currentTime;
		}

		return 0L;
	}
}
