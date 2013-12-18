package com.examples.with.different.packagename.staticfield;

public class StaticFooProxy {

	public StaticFooProxy() {
	}

	public static boolean bar(int x) throws IllegalStateException {
		boolean ret_val = StaticFoo.bar(x);
		if (ret_val)
			return true;
		else
			return false;
	}

}
