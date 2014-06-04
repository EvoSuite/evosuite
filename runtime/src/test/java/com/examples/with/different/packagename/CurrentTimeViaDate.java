/**
 * 
 */
package com.examples.with.different.packagename;

import java.util.Date;

/**
 * @author Gordon Fraser
 * 
 */
public class CurrentTimeViaDate {
	public long getCurrentTime(long time) {
		Date date = new Date();
		long currentTime = date.getTime();
		if (time == currentTime) {
			return currentTime;
		}

		return 0L;
	}
}
