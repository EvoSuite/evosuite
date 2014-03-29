package com.examples.with.different.packagename.stable;

public class Singleton {
	
	private int counter = 0;

	public int nextId() {
		return counter++;
	}
}
