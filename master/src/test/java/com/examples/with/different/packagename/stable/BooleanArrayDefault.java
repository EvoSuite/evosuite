package com.examples.with.different.packagename.stable;

public class BooleanArrayDefault {

	private final Boolean[] booleanArray;

	public BooleanArrayDefault(Boolean[] myBooleanArray) {
		this.booleanArray = myBooleanArray;
	}

	public boolean isEmpty() {
		return this.booleanArray.length == 0;
	}

	public boolean isNull() {
		for (int i = 0; i < booleanArray.length; i++) {
			Boolean f = booleanArray[i];
			if (f != null)
				return false;
		}
		return true;
	}
	
	public String printArray() {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < booleanArray.length; i++) {
			Boolean f = booleanArray[i];
			String f_str = f.toString();
			b.append(f_str);
		}
		return b.toString();
	}
}
