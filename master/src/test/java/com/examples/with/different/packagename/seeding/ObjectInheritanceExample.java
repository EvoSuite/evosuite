package com.examples.with.different.packagename.seeding;

public class ObjectInheritanceExample {

	public boolean testMe(Object o) {
		A a = (A)o;
		if(a.fooBar())
			return true;
		else
			return false;
		
	}
	
}
