package com.examples.with.different.packagename.staticfield;

public class PartialClassInit {

	private static boolean flag=false;
	
	private static PartialClassInit instance;

	private static float myFloat;
	
	private static int myInt = 0;

	public static boolean bar(int value) throws IllegalStateException {
		if (instance != null)
			throw new IllegalStateException("Only one call to bar is allowed");

		instance= new PartialClassInit();
		if (value < 0)
			return true;
		else
			return false;
	}
	
}
