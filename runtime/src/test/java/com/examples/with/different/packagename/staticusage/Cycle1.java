package com.examples.with.different.packagename.staticusage;

public class Cycle1 {

	public static int cycle1Method() {
		return Cycle2.cycle2Method();
	}
}
