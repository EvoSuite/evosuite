package com.examples.with.different.packagename;

import java.util.Locale;

public class TypeSeedingExampleLocale {

	public boolean testMe(Object o) {
		if(o instanceof Locale) 
			return true;
		else 
			return false;
	}
	
}
