package com.examples.with.different.packagename.clinit;

public class FinalReferenceField {

	public static final Object final_value = new Object();
	
	public boolean coverMe() {
		if (final_value != null) {
			//unreachable
			return false;
		} else {
			return true;
		}
	}
}
