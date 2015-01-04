package com.examples.with.different.packagename.contracts;

public class HashcodeException {

	@Override
	public int hashCode() {
		throw new RuntimeException("Test!");
	}
}
