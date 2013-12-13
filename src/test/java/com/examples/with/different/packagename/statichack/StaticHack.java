package com.examples.with.different.packagename.statichack;

public class StaticHack {

	private static int counter = 0;
	
	public static int inc() throws IllegalStateException{
		counter++;
		throw new IllegalStateException();
	}
	
	public static int reset() throws IllegalStateException{
		counter=0;
		throw new IllegalStateException("Cannot invoke reset()");
	}
	
	public boolean checkCounter() {
		if (counter==0) {
			return true;
		} else {
			return false;
		}
	}
	
}
