package com.examples.with.different.packagename.staticfield;

public class NoClassInit {

	private static NoClassInit instance;

	private static boolean flag;

	private static float myFloat;
	
	private static int myInt;

	private static NoClassInit[] myArray;

	private static short myShort;

	private static char myChar;

	public static boolean bar(int value) throws IllegalStateException {
		if (instance != null)
			throw new IllegalStateException("Only one call to bar is allowed");

		instance= new NoClassInit();
		if (value < 0)
			return true;
		else
			return false;
	}
	
}
