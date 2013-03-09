package com.examples.with.different.packagename;

public class TimeOperation {

	public boolean testMe() {
		long time = System.currentTimeMillis();
		if(time == 100) {
			return true;
		}
		return false;
	}
	
}
