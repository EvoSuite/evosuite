package com.examples.with.different.packagename.contracts;

public class RaiseNullPointerException {

	public void foo(int x) {
		if(x == 42)
			throw new NullPointerException();		
	}
}
