package com.examples.with.different.packagename;

import java.util.GregorianCalendar;

public class CurrentTimeViaGregorianCalendar {
	public long getCurrentTime(long time) {
		GregorianCalendar calendar = new GregorianCalendar();
		long currentTime = calendar.getTimeInMillis();
		if (time == currentTime) {
			return currentTime;
		}

		return 0L;
	}
}
