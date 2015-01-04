package com.examples.with.different.packagename.testcarver;

import java.util.Locale;

public class ClassDependingOnStaticFieldInOtherClass {

	public boolean testMe(Locale other) {
		if(other.equals(StaticFieldInOtherClass.x))
			return true;
		else
			return false;
	}
}
