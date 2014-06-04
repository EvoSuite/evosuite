package com.examples.with.different.packagename;

import java.util.Locale;

public class TypeSeedingExampleGeneric<T> {

	public boolean testMe(T type) {
		if(type instanceof Locale)
			return true;
		else
			return false;
	}
	
}
