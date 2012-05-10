package com.examples.with.different.packagename;

public class StaticPrinting {

	public static final String MESSAGE = "ERROR: this should not be printed";
	public static String foo;
	
	static{
		System.out.println(MESSAGE);
		foo = MESSAGE;
	}
	
	public void print(){
		System.out.println(foo);
	}
	
}
