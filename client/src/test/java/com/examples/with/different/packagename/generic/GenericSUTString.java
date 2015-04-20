package com.examples.with.different.packagename.generic;

public class GenericSUTString<T> {

	public boolean testMe(T t) {
		String x = (String)t;
		if(x.equals("test"))
			return true;
		else
			return false;
	}
	
}
