package com.examples.with.different.packagename.seeding;


public class TypeExample {

	public class X {};
	
	public boolean testMe(Object o) {
		if(o instanceof ObjectCastExample) 
			return true;
		else 
			return false;
	}
}
