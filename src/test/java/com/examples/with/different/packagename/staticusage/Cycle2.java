package com.examples.with.different.packagename.staticusage;

public class Cycle2 {

	public static int cycle2Method() {
		return Cycle1.cycle1Method();
	}

}
