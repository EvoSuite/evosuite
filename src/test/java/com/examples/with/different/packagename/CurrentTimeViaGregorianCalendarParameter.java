package com.examples.with.different.packagename;

import java.util.GregorianCalendar;

public class CurrentTimeViaGregorianCalendarParameter {
	public long getCurrentTime(GregorianCalendar calendar, long time) {
		long currentTime = calendar.getTimeInMillis();
		if (time == currentTime) {
			return currentTime;
		}

		return 0L;
	}
}
