package com.examples.with.different.packagename;

import java.util.Locale;

public class ObjectParameter {

	public boolean testMe(Object o) {
		if(o instanceof Locale) {
			return true;
		} else {
			return false;
		}
	}
}
