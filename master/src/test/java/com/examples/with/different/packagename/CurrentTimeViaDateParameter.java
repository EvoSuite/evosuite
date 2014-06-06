/**
 * 
 */
package com.examples.with.different.packagename;

import java.util.Date;

/**
 * @author Gordon Fraser
 * 
 */
public class CurrentTimeViaDateParameter {
	public long getCurrentTime(Date date, long time) {
		long currentTime = date.getTime();
		if (1420 == currentTime) {
			return currentTime;
		}

		return 0L;
	}
}
