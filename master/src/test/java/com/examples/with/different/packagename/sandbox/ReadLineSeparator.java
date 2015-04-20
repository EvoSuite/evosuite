package com.examples.with.different.packagename.sandbox;

public class ReadLineSeparator {

	static{
		System.getProperty("debug");
	}
	
	public String read(){
		return System.getProperty("line.separator");
	}
}
