package com.examples.with.different.packagename.reset;

public class StaticInitCatchNullPointer {

	static {
		try {
			throw new NullPointerException("A null pointer exception!");
		} catch (NullPointerException e) {
			//expected
		}
	}
	

}
