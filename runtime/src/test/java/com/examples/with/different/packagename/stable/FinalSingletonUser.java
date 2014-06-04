package com.examples.with.different.packagename.stable;

public class FinalSingletonUser {

	private final static Singleton counter = new Singleton();
	
	private final int myId;
	public FinalSingletonUser() {
		myId = counter.nextId();
	}
	
	public boolean isZero() {
		if (myId==0)
			return true;
		else
			return false;
	}
	
	public boolean isNotZero() {
		if (myId!=0)
			return true;
		else
			return false;
	}
	
}
