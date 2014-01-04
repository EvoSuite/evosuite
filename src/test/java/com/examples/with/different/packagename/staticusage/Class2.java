package com.examples.with.different.packagename.staticusage;

public class Class2 {

	private static int myIntField;
	
	public static void initFields() {
		myIntField = Integer.MAX_VALUE;
	}
	
	public static void clearFields() {
		myIntField = 0;
	}
	
	public static int getIntField() {
		return myIntField;
	}
}
