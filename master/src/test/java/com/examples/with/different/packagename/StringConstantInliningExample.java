package com.examples.with.different.packagename;

public class StringConstantInliningExample {

	public boolean foo(String x) {
		if(x.endsWith(".class"))
			return true;
		else
			return false;
	}
}
