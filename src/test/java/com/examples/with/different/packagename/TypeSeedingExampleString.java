package com.examples.with.different.packagename;

public class TypeSeedingExampleString {

	
	public boolean testMe(Object o) {
		if(o instanceof String) {
			if(((String)o).equals("test")) {
				return true;
			}
		}
		
		return false;
	}
}
