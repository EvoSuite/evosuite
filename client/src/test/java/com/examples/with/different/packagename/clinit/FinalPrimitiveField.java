package com.examples.with.different.packagename.clinit;

public class FinalPrimitiveField {

	public static final int final_value = 10;
	
	public boolean coverMe() {
		if (final_value != 10) {
			//unreachable
			return false;
		} else {
			return true;
		}
	}
}
