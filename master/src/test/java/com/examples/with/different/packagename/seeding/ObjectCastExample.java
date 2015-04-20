package com.examples.with.different.packagename.seeding;

public class ObjectCastExample {

	public boolean testMe(Object o) {
		String s = (String)o;
		if(s.equals("foobar"))
			return true;
		else
			return false;
	}
}
