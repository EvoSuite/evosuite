package com.examples.with.different.packagename.testcarver;

import java.util.Locale;

public class ClassWithPublicField {

	public Locale x = Locale.CHINESE;
	
	public boolean testMe(Locale other) {
		if(other.equals(x))
			return true;
		else
			return false;
	}
}
