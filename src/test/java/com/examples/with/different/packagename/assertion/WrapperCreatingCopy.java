package com.examples.with.different.packagename.assertion;

public class WrapperCreatingCopy {
	public Integer foo(Integer x) {
		return new Integer(x.intValue());
	}
}
