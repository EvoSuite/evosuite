package com.examples.with.different.packagename;

public class CurrentTime {

	public long getCurrentTime(long time) {
		long currentTime = System.currentTimeMillis();
		if(time == currentTime) {
			return currentTime;
		}
		
		return 0L;
	}
	
}
